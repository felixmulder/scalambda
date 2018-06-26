package lambda.swaggy

import io.swagger.models._
import io.swagger.models.properties._
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
          q"import io.circe.{Decoder, Encoder}",
          q"import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}",
          q"import io.circe.java8.time._",
          q"import java.time.Instant",
        ),
        q"final case class $tpname(..${constructorParams(model)})",
        q"""
          object ${Term.Name(name)} {
            implicit val decoder: Decoder[$tpname] =
              deriveDecoder[$tpname]

            implicit val encoder: Encoder[$tpname] =
              deriveEncoder[$tpname]
          }
        """,
      )
    }
    .toList

  private def constructorParams(m: Model): List[Term.Param] = m match {
    case m: ModelImpl if m.getType == "object" =>
      m.getProperties.asScala.map { case (name, tpe) =>
        Term.Param(
          Nil, Name(name), Some(propertyToType(tpe)), None
        )
      }
      .toList
    case _ => Nil
  }

  private def propertyToType(prop: Property): Type = prop match {
    case p: ArrayProperty => Type.Name(s"Array[${propertyToType(p.getItems)}]")
    case _: BooleanProperty => Type.Name("Boolean")
    case _: DoubleProperty => Type.Name("Double")
    case p: DateProperty if p.getType == "string" => Type.Name("LocalDate")
    case p: DateTimeProperty if p.getType == "string" => Type.Name("Instant")
    case _: FloatProperty => Type.Name("Float")
    case _: IntegerProperty => Type.Name("Int")
    case _: LongProperty => Type.Name("Long")
    case _: PasswordProperty => Type.Name("String")
    case p: RefProperty => Type.Name(p.getSimpleRef)
    case _: StringProperty => Type.Name("String")
  }
}
