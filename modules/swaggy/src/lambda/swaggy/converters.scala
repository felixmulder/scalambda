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
