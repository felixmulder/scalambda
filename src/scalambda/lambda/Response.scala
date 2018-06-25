package scalambda.lambda

import io.circe.Encoder
import io.circe.syntax._
import io.circe.generic.semiauto.deriveEncoder

case class Response[A](statusCode: Int, headers: Map[String, String], body: A)

object Response {
  private val encoderStringRes: Encoder[Response[String]] =
    deriveEncoder

  implicit def encoder[A: Encoder]: Encoder[Response[A]] =
    Encoder.instance[Response[A]] { res =>
      res.copy(body = res.body.asJson.noSpaces).asJson
    }
}


