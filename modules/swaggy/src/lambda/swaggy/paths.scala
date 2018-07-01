package lambda.swaggy

import io.swagger.models._
import io.swagger.models.HttpMethod._
import io.swagger.models.parameters.{BodyParameter, QueryParameter}
import scala.collection.JavaConverters._
import scala.meta._
import cats.implicits._
import cats.data.{Validated, NonEmptyList => NEL}

import converters._
import error._

object paths {

  final case class Endpoint(name: String, path: String, operations: List[Handler])

  sealed trait Handler {
    def name: String
    def path: String
  }

  final case class Get(name: String, path: String, queryParams: Map[Term, Type])
    extends Handler

  final case class Head(name: String, path: String, queryParams: Map[Term, Type])
    extends Handler

  final case class Post(name: String, path: String, body: Type)
    extends Handler

  final case class Put(name: String, path: String, body: Type)
    extends Handler

  final case class Delete(name: String, path: String)
    extends Handler

  final case class Options(name: String, path: String)
    extends Handler

  final case class Patch(name: String, path: String, body: Type)
    extends Handler

  def apply(swagger: Swagger, pkg: List[String]): ErrorsOr[List[Endpoint]] = {
    def addEndpoint(map: Map[String, ErrorsOr[Endpoint]], ptup: (String, Path)) = {
      val (pathString, path) = ptup
      val name = endpointName(pathString)
      val ops  = getOps(path, pathString)
      val endpoint = map.getOrElse(name, Endpoint(name, pathString, Nil).valid)
      map + (name -> endpoint.andThen { e =>
        ops.map { ops =>
          e.copy(operations = e.operations ++ ops)
        }
      })
    }

    swagger.getPaths.asScala.foldLeft(Map.empty[String, ErrorsOr[Endpoint]])(addEndpoint)
      .values.toList.sequence
  }

  private[swaggy] def getOps(path: Path, pathString: String): ErrorsOr[List[Handler]] =
    path
      .getOperationMap.asScala
      .map { case (method, op) =>
        val name   = op.getOperationId
        val body   = bodyParam(op)
        val params = queryParams(op)

        def orMissing[A](f: Type.Name => A): ErrorsOr[A] =
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
            Type.Name(s"Array[${propertyToType(q.getItems)}]")
          else
            typeToType(q.getType, Option(q.getFormat))
        (Term.Name(q.getName), tpe)
    }
    .toMap

  private[swaggy] def bodyParam(op: Operation): Option[Type.Name] =
    op.getParameters.asScala.collectFirst {
      case p: BodyParameter =>
        p.getSchema match {
          case r: RefModel =>
            Type.Name(r.getReference.split("/").last)

          case r: ArrayModel =>
            Type.Name(s"Array[${propertyToType(r.getItems)}]")
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
      .map(_.capitalize).mkString("", "", ".scala")
}
