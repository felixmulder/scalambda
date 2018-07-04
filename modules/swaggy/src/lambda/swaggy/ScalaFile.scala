package lambda.swaggy

import scala.meta._
import cats.data.NonEmptyList

import syntax._

sealed trait ScalaFile {
  def pkg: NonEmptyList[String]
  def title: String
}

final case class RequestType(
  title: String,
  pkg: NonEmptyList[String],
  imports: List[Import],
  cls: Defn.Class, obj: Defn.Object
) extends ScalaFile

final case class Endpoint(
  title: String,
  path: String,
  pkg: NonEmptyList[String],
  handlers: List[MethodHandler]
) extends ScalaFile

final case class JsonHandler(
  title: String,
  handlerTitle: String,
  pkg: NonEmptyList[String],
  handler: MethodHandler
) extends ScalaFile

object ScalaFile {
  def toTree(f: ScalaFile): Tree =
    f match {
      case f: RequestType =>
        pkg(f.pkg, f.imports :+ f.cls :+ f.obj)
      case f: Endpoint =>
        endpoint(f)
      case f: JsonHandler =>
        jsonHandler(f)
    }

  private def pkg(name: NonEmptyList[String], stats: List[Stat]): Tree =
    q"package ${name.toList.mkString(".").term} { ..$stats }"

  private def endpoint(e: Endpoint): Tree =
    pkg(e.pkg, endpointStats(e))

  private def endpointStats(e: Endpoint): List[Stat] =
    endpointImports(e.pkg) ++ endpointObject(e)

  private def endpointObject(e: Endpoint): List[Defn.Object] =
    q"object ${e.title.term} { ..${endpointMethods(e)} }" :: Nil

  private def endpointMethods(e: Endpoint): List[Defn.Def] =
    e.handlers.map(endpointMethod)

  private[swaggy] def endpointMethod(m: MethodHandler): Defn.Def =
    m match {
      case h: Post =>
        q"def ${h.name.term}(request: Request[${h.body}]): IO[Response[Json]] = ???"

      case h: Put =>
        q"def ${h.name.term}(request: Request[${h.body}]): IO[Response[Json]] = ???"

      case h: Patch =>
        q"def ${h.name.term}(request: Request[${h.body}]): IO[Response[Json]] = ???"

      case h =>
        q"def ${h.name.term}(request: Request[EmptyBody]): IO[Response[Json]] = ???"

      //case h: Head =>
      //  ??? // could be implemented in terms of the GET, mapping away the body

      //case h: Delete => ???

      //case h: Options =>
    }

  private def endpointImports(path: NonEmptyList[String]): List[Import] = {
    val pkg: Term.Ref = termRef(path.init, "models".term)
    List(
      q"import cats.effect.IO",
      q"import io.circe.Json",
      q"import lambda.{EmptyBody, Request, Response}",
      q"import $pkg._",
    )
  }


  private def termRef(path: List[String], pkg: Term.Name): Term.Ref = {
    def inner(xs: List[String], last: Term.Name): Term.Ref =
      xs match {
        case x :: xs => Term.Select(inner(xs, x.term), last)
        case Nil => last
      }

    if (path.isEmpty) pkg
    else inner(path, pkg)
  }

  private def jsonHandler(h: JsonHandler): Tree =
    pkg(h.pkg, jsonHandlerStats(h))

  private def jsonHandlerStats(h: JsonHandler): List[Stat] =
    jsonHandlerImports(h.pkg) ++ jsonHandlerClass(h)

  private def jsonHandlerImports(path: NonEmptyList[String]): List[Import] = {
    //val models = termRef(path.init, "models".term)
    val endpoints = termRef(path.init, "endpoints".term)

    val models = if (path.init.isEmpty) Nil
    else {
      val prefix = termRef(path.init.dropRight(1), path.init.toList.last.term)
      List(
        q"import $prefix.models"
      )
    }

    List(
      q"import cats.effect.IO",
      q"import io.circe.Json",
      q"import io.circe.syntax._",
      q"import lambda.{EmptyBody, Lambda, Request, Response}",
      q"import $endpoints._",
    ) ++ models
  }

  private def jsonHandlerClass(h: JsonHandler): List[Stat] = {
    val tpe: Type = h.handler match {
      case h: MethodHandlerWithBody =>
        h.body match {
          case Type.Apply(Type.Name("List"), Type.Name(inner) :: Nil) =>
            ("models." + inner).listTpe
          case tpe if tpe == EmptyBody => EmptyBody
          case Type.Name(value) =>
            ("models." + value).tpe
        }
      case _ => EmptyBody
    }

    q"""
      class ${h.title.tpe} extends Lambda[IO, Request[$tpe], Response[Json]] {
        override def process(request: Request[$tpe]): IO[Response[Json]] =
          ${s"${h.handlerTitle}.${h.handler.name}".term}(request)
      }
    """ :: Nil
  }
}
