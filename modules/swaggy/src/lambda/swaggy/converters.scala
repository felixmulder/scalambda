package lambda.swaggy

import scala.meta.Type
import io.swagger.models.properties._

/** A bunch of shared converters for generating scala definitions from the
 *  swagger-core library
 */
object converters {

  /** Convert a Swagger Property into a scala.meta.Type */
  def propertyToType(prop: Property): Type = prop match {
    case p: ArrayProperty => Type.Name(s"Array[${propertyToType(p.getItems)}]")
    case _: BooleanProperty => boolean
    case _: DoubleProperty => double
    case p: DateProperty if p.getType == "string" => localDate
    case p: DateTimeProperty if p.getType == "string" => instant
    case _: FloatProperty => float
    case _: IntegerProperty => int
    case _: LongProperty => long
    case _: PasswordProperty => string
    case p: RefProperty => Type.Name(p.getSimpleRef)
    case _: StringProperty => string
  }

  def typeToType(tpe: String, format: Option[String]): Type = (tpe, format) match {
    case ("string", _) => string
    case ("integer", _) => int
    case _ => ???
  }

  private val boolean = Type.Name("Boolean")
  private val double = Type.Name("Double")
  private val localDate = Type.Name("LocalDate")
  private val instant = Type.Name("Instant")
  private val int = Type.Name("Int")
  private val float = Type.Name("Float")
  private val long = Type.Name("Long")
  private val string = Type.Name("String")
}
