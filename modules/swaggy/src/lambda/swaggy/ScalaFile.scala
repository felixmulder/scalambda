package lambda.swaggy

import org.scalafmt.Scalafmt.format
import scala.meta._

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

