package lambda.swaggy.error

import io.swagger.models.HttpMethod

abstract class SwaggyError(msg: String) extends RuntimeException(msg)

final case class MissingBody(path: String, method: HttpMethod)
  extends SwaggyError(s"Missing body for $method in $path")

final case class UnknownType(tpe: String, format: Option[String])
  extends SwaggyError(s"Unknown type: $tpe, with format: $format")
