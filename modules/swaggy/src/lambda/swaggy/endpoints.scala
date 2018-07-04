package lambda.swaggy

import io.swagger.models._
import io.swagger.models.HttpMethod._
import io.swagger.models.parameters.{BodyParameter, QueryParameter}
import scala.collection.JavaConverters._
import scala.meta._
import scala.meta.contrib._
import cats.implicits._
import cats.data.{Validated, NonEmptyList => NEL}

import metasyntax._
import converters._
import error._

object endpoints {

  def apply(swagger: Swagger, pkg: NEL[String]): ErrorsOr[List[Endpoint]] = {
    def addEndpoint(map: Map[String, ErrorsOr[Endpoint]], ptup: (String, Path)) = {
      val (pathString, path) = ptup
      val name = endpointName(pathString)
      val ops  = getOps(path, pathString)
      val endpoint = map.getOrElse(name, Endpoint(name, pathString, pkg, Nil).valid)
      map + (name -> endpoint.andThen { e =>
        ops.map { ops =>
          e.copy(handlers = e.handlers ++ ops)
        }
      })
    }

    swagger.getPaths.asScala.foldLeft(Map.empty[String, ErrorsOr[Endpoint]])(addEndpoint)
      .values.toList.sequence
  }

  def adaptExisting(existing: List[(Endpoint, Option[String])]): ErrorsOr[List[(String, Tree)]] = {
    val shouldAdapt = existing.collect { case (e, Some(s)) => (e, s) }
    val oldTs = parseExisting(shouldAdapt.map(snd))
      .andThen { trees =>
        shouldAdapt
          .map(fst)
          .zip(trees)
          .map(adaptTree.tupled)
          .sequence
      }

    val newTs = existing
      .filter(snd(_).isEmpty)
      .map(fst)
      .map(ep => (ep, ScalaFile.toTree(ep)))

    oldTs.map(_ ++ newTs).map(fstToFileName)
  }

  private[swaggy] def fstToFileName[A](xs: List[(Endpoint, A)]): List[(String, A)] =
    xs.map {
      case (k, v) => (s"${k.pkg.toList.mkString("/")}/${k.title}.scala", v)
    }

  private[swaggy] val adaptTree: (Endpoint, Tree) => ErrorsOr[(Endpoint, Tree)] =
    (ep, tree) => {
      def adaptedStats(xs: List[Stat]): List[Stat] = {
        val handlers =
          ep.handlers.groupBy(_.name)
            .mapValues(_.map(ScalaFile.endpointMethod))

        val defs = xs.collect { case d: Defn.Def => d }
        val other = xs.filterNot(_.isInstanceOf[Defn.Def])

        val defMap = defs.map(_.name.value).toSet
        val nonExistingHandlers = handlers.filterKeys(!defMap.contains(_))

        defs ++ nonExistingHandlers.flatMap(snd) ++ other
      }

      val obj = tree.find {
        case obj: Defn.Object if obj.name.value == ep.title => true
        case _ => false
      }

      if (obj.isDefined)
        (ep -> tree.transform {
          case obj: Defn.Object if obj.name.value == ep.title =>
            obj.copy(
              templ = obj.templ.copy(
                stats = adaptedStats(obj.templ.stats)
              )
            )
        })
        .validNel
        .map {
          // This is needed until: https://github.com/scalameta/scalameta/pull/1527
          case (ep, Source(trees)) => (ep, Source(trees))
          case (ep, tree) => (ep, tree)
        }
      else MissingObject(ep.title).invalidNel
    }

  /** Parse fileBodies into trees */
  private[swaggy] def parseExisting(existingMap: List[String]): ErrorsOr[List[Tree]] =
    existingMap
      .map(_.parse[Source].toEither.toValidatedNel)
      .sequence
      .leftMap(_.map(e => ParseError(e)))

  private[swaggy] def getOps(path: Path, pathString: String): ErrorsOr[List[MethodHandler]] =
    path
      .getOperationMap.asScala
      .map { case (method, op) =>
        val name   = op.getOperationId
        val body   = bodyParam(op)
        val params = queryParams(op)

        def orMissing[A](f: Type => A): ErrorsOr[A] =
          Validated.fromOption(body.map(f), NEL.of(MissingBody(pathString, method)))

        method match {
          case GET =>
            Get(name, pathString, params).validNel
          case HEAD =>
            Head(name, pathString, params).validNel
          case POST =>
            Post(name, pathString, body.getOrElse(EmptyBody)).validNel
          case DELETE =>
            Delete(name, pathString).validNel
          case OPTIONS =>
            Options(name, pathString).validNel
          case PUT =>
            orMissing(Put(name, pathString, _))
          case PATCH =>
            orMissing(Patch(name, pathString, _))
        }
      }
      .toList.sequence

  private[swaggy] def queryParams(op: Operation): Map[Term, Type] =
    op.getParameters.asScala.collect {
      case q: QueryParameter =>
        val tpe =
          if (q.getType == "array")
            Type.Apply("List".tpe, propertyToType(q.getItems) :: Nil)
          else
            typeToType(q.getType, Option(q.getFormat))
        (Term.Name(q.getName), tpe)
    }
    .toMap

  private[swaggy] def bodyParam(op: Operation): Option[Type] =
    op.getParameters.asScala.collectFirst {
      case p: BodyParameter =>
        p.getSchema match {
          case r: RefModel =>
            Type.Name(r.getReference.split("/").last)

          case r: ArrayModel =>
            Type.Apply("List".tpe, propertyToType(r.getItems) :: Nil)
        }
    }

  /** Create an endpoint name from the given path string
   *
   *  Will currently remove path parameters in order to create a terser name
   */
  private[swaggy] def endpointName(path: String): String =
    path
      .split("/")
      .filter(_.nonEmpty)
      .filterNot(_.head == '{')
      .map(_.capitalize).mkString
}
