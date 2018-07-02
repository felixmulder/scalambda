package lambda.swaggy

import org.scalafmt.Scalafmt.format
import scala.meta._
import cats.data.NonEmptyList

import metasyntax._

sealed trait ScalaFile {
  def pkg: NonEmptyList[String]
  def title: String
}

final case class DomainType(
  title: String,
  pkg: NonEmptyList[String],
  imports: List[Import],
  cls: Defn.Class, obj: Defn.Object
) extends ScalaFile

final case class Endpoint(
  title: String,
  path: String,
  pkg: NonEmptyList[String],
  handlers: List[Handler]
) extends ScalaFile

object ScalaFile {
  def contents(f: ScalaFile): Either[Throwable, Array[Byte]] =
    format {
      f match {
        case f: DomainType =>
          pkg(f.pkg, f.imports :+ f.cls :+ f.obj)
        case f: Endpoint =>
          handler(f)
      }
    }
    .toEither.map(_.getBytes)

  private def pkg(name: NonEmptyList[String], stats: List[Stat]): String =
    q"package ${name.toList.mkString(".").term} { ..$stats }".syntax

  private def handler(e: Endpoint): String =
    pkg(e.pkg, handlerStats(e))

  private def handlerStats(e: Endpoint): List[Stat] =
    handlerImports(e.pkg) ++ handlerObject(e)

  private def handlerObject(e: Endpoint): List[Defn.Object] =
    q"object ${e.title.term} { ..${handlerMethods(e)} }" :: Nil

  private def handlerMethods(e: Endpoint): List[Defn.Def] =
    e.handlers.map {
      case h: Post =>
        q"def ${h.name.term}(body: Request[${h.body}]): IO[String] = ???"

      case h: Put =>
        q"def ${h.name.term}(body: Request[${h.body}]): IO[String] = ???"

      case h: Patch =>
        q"def ${h.name.term}(body: Request[${h.body}]): IO[String] = ???"

      case h =>
        q"def ${h.name.term}: IO[String] = ???"

      //case h: Head =>
      //  ??? // could be implemented in terms of the GET, mapping away the body

      //case h: Delete => ???

      //case h: Options =>
    }

  private def handlerImports(path: NonEmptyList[String]) = {
    val pkg: Term.Ref = selectFrom(path.init, "models".term)
    List(
      q"import cats.effect.IO",
      q"import lambda.{EmptyBody, Request}",
      q"import $pkg._",
    )
  }

  private def selectFrom(path: List[String], pkg: Term.Name): Term.Ref = {
    def inner(xs: List[String], last: Term.Name): Term.Ref =
      xs match {
        case x :: xs => Term.Select(inner(xs, x.term), last)
        case Nil => last
      }

    if (path.isEmpty) Term.Select("_root_".term, pkg)
    else inner(path, pkg)
  }
}
