package lambda.swaggy

import cats.data.{NonEmptyList => NEL}
import cats.implicits._

object handlers {
  def apply(endpoints: List[Endpoint], pkg: NEL[String]): List[JsonHandler] =
    endpoints.flatMap(flattenOut(_, pkg))

  private[swaggy] def flattenOut(e: Endpoint, pkg: NEL[String]) =
    e.handlers.map { handler =>
      JsonHandler(e.title, pkg, handler)
    }
}
