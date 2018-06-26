package lambda.swaggy

import scala.meta._

sealed trait ScalaFile {
  def pkg: List[String]
  def title: String
  def contents: Array[Byte]
}

final case class DomainType(
  title: String,
  pkg: List[String],
  imports: List[Import],
  cls: Defn.Class, obj: Defn.Object
) extends ScalaFile {
  def contents =
    q"""
      package ${Term.Name(pkg.mkString("."))} {
        ..${imports :+ cls :+ obj}
      }
    """.syntax.getBytes
}

