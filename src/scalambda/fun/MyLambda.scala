package scalambda.fun

import cats.effect.IO
import io.circe.generic.JsonCodec
import io.circe.Json
import io.circe.syntax._

import scalambda.lambda._

@JsonCodec
case class Car(year: Int, model: String)

//class HelloWorld extends Lambda[IO, Car, Car] {
//  def process(car: Car): IO[Car] =
//    IO { car.copy(car.year / 2, car.model.map(_.toUpper)) }
//}

class HelloWorld extends JsonLambda[IO] {
  def process(json: Json): IO[Json] = {
    println(json.spaces4)
    IO.pure(Response(200, Map("Content-Type" -> "application/json"), json.noSpaces).asJson)
  }
}
