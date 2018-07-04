package lambda.swaggy.error

import cats.data.NonEmptyList
import io.swagger.models.HttpMethod
import scala.meta.Parsed

sealed abstract class SwaggyError(msg: String)
  extends RuntimeException(msg)

final case class MultipleErrors(errors: NonEmptyList[SwaggyError])
  extends SwaggyError(errors.map(_.getMessage).toList.mkString("\n"))

final case class MissingObject(title: String)
  extends SwaggyError(s"Missing object definition in endpoint: $title.scala")

final case class MissingBody(path: String, method: HttpMethod)
  extends SwaggyError(s"Missing body for $method in $path")

final case class UnknownType(tpe: String, format: Option[String])
  extends SwaggyError(s"Unknown type: $tpe, with format: $format")

final case class ParseError(err: Parsed.Error)
  extends SwaggyError(err.toString)
