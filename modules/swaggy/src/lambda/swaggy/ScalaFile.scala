package lambda.swaggy

import org.scalafmt.Scalafmt.format
import scala.meta._
import metasyntax._

sealed trait ScalaFile {
  def pkg: List[String]
  def title: String
  def contents: Either[Throwable, Array[Byte]]
}

final case class DomainType(
  title: String,
  pkg: List[String],
  imports: List[Import],
  cls: Defn.Class, obj: Defn.Object
) extends ScalaFile {
  def contents =
    format(
      q"""
        package ${Term.Name(pkg.mkString("."))} {
          ..${imports :+ cls :+ obj}
        }
      """.syntax
    )
    .toEither.map(_.getBytes)
}

final case class Endpoint(
  title: String,
  path: String,
  pkg: List[String],
  handlers: List[Handler]
) extends ScalaFile {

  private[this] def imports: List[Import] =
    List(
      q"import cats.effect.IO",
    )

  private[this] def cls =
    q"""
      class ${Type.Name(title)} {
        ..${methods}
      }
    """

  private[this] def methods =
    handlers.map {
      case h: Post =>
        q"def ${h.name.term}(body: ${h.body}): IO[String] = ???"

      case h: Put =>
        q"def ${h.name.term}(body: ${h.body}): IO[String] = ???"

      case h: Patch =>
        q"def ${h.name.term}(body: ${h.body}): IO[String] = ???"

      case h =>
        q"def ${h.name.term}: IO[String] = ???"

      //case h: Head =>
      //  ??? // could be implemented in terms of the GET, mapping away the body

      //case h: Delete => ???

      //case h: Options =>
    }

  def contents =
    format {
      q"""
      package ${Term.Name(pkg.mkString("."))} {
        ..${imports :+ cls}
      }
      """.syntax
    }
    .toEither.map(_.getBytes)
}
