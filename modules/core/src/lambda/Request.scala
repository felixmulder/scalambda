package lambda

import java.util.UUID
import java.time.Instant
import java.time.format.DateTimeFormatter

import io.circe.syntax._
import io.circe.Decoder
import io.circe.parser.decode
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.extras.semiauto.deriveUnwrappedDecoder
import io.circe.java8.time._

final case class Method(value: String) extends AnyVal

object Method {
  implicit val decoder: Decoder[Method] = deriveUnwrappedDecoder
}


final case class Headers(val value: Map[String, String]) extends AnyVal

object Headers {
  implicit val decoder: Decoder[Headers] = deriveUnwrappedDecoder
}

final case class QueryStringParameters(value: Map[String, String]) extends AnyVal

object QueryStringParameters {
  implicit val decoder: Decoder[QueryStringParameters] =
    Decoder[Option[Map[String, String]]].map {
      _.fold(QueryStringParameters(Map.empty))(QueryStringParameters.apply)
    }
}

final case class PathParameters(value: Map[String, String]) extends AnyVal

object PathParameters {
  implicit val decoder: Decoder[PathParameters] = deriveUnwrappedDecoder
}

final case class RequestContext(
  resourceId: String,
  resourcePath: String,
  httpMethod: String,
  extendedRequestId: String,
  requestTime: Instant,
  path: String,
  accountId: String,
  protocol: String,
  stage: String,
  requestId: UUID,
  identity: Identity,
  apiId: String,
)

object RequestContext {
  // An example time received from AWS: 24/Jun/2018:17:57:52 +0000
  private implicit val decodeInstant =
    decodeZonedDateTime(DateTimeFormatter.ofPattern("dd/MMM/yyyy:kk:mm:ss Z"))
      .map(_.toInstant)

  implicit val decoder: Decoder[RequestContext] = deriveDecoder
}

final case class Identity(
  sourceIp: String,
  userAgent: String,
)

object Identity {
  implicit val decoder: Decoder[Identity] = deriveDecoder
}

case class Request[A](
  resource: String,
  path: String,
  httpMethod: Method,
  headers: Headers,
  queryStringParameters: QueryStringParameters,
  pathParameters: PathParameters,
  requestContext: RequestContext,
  body: A,
)

object Request {
  /** Request comes with stringified body, this decoder takes that into account
   */
  implicit def decoder[A: Decoder]: Decoder[Request[A]] =
    deriveDecoder[Request[String]].emap { req =>
      decode[A](req.body).map(body => req.copy(body = body)).left.map(_.getMessage)
    }
}
