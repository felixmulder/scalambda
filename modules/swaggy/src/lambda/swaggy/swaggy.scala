package lambda

import cats.data.{Validated, ValidatedNel}
import scala.meta.Type

import lambda.swaggy.error._

package object swaggy {

  type ErrorOr[A] = Validated[SwaggyError, A]
  type ErrorsOr[A] = ValidatedNel[SwaggyError, A]

  private[swaggy] val EmptyBody: Type.Name = Type.Name("EmptyBody")

  private[swaggy] def fst[A,B](t: (A, B)): A = t._1
  private[swaggy] def snd[A,B](t: (A, B)): B = t._2

}
