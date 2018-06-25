package lambda

import cats.implicits._
import cats.effect.Effect
import io.circe.{Encoder, Decoder, Json}

import Request._

abstract class Lambda[F[_], I, O](
  implicit
  D: Decoder[I],
  E: Encoder[O],
  F: Effect[F]
) extends JsonLambda[F] {

  def process(request: I): F[O]

  final def process(json: Json): F[Json] =
    json
      .as[I]
      .map(process(_).map(E.apply))
      .valueOr(throw _)
}
