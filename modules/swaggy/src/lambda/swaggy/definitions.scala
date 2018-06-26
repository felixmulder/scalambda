package lambda.swaggy

import io.swagger.models._
import scala.meta._
import scala.collection.JavaConverters._

object definitions {

  def apply(swagger: Swagger, pkg: List[String]): List[DomainType] =
    swagger.getDefinitions.asScala.map { case (name, model) =>
      val tpname = Type.Name(name)
      DomainType(
        name + ".scala",
        pkg,
        List(
          q"import io.circe.Decoder",
          q"import io.circe.generic.semiauto.deriveDecoder",
        ),
        q"final case class $tpname()",
        q"""
          object ${Term.Name(name)} {
            implicit val decoder: Decoder[$tpname] =
              deriveDecoder[$tpname]
          }
        """,
      )
    }
    .toList


  private def printModel(m: Model): Unit = m match {
    case m: RefModel =>
      println(m.getProperties)
      println(m.getSimpleRef)
      println(m.getExample)
  }
}
