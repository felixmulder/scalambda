package lambda

import cats.data.{Validated, ValidatedNel}
import scala.meta.Type

import lambda.swaggy.error._

package object swaggy {

  type ErrorOr[A] = Validated[SwaggyError, A]
  type ErrorsOr[A] = ValidatedNel[SwaggyError, A]

  val EmptyBody: Type = Type.Name("EmptyBody")

}
