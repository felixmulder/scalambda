package lambda.swaggy

import cats.effect.Effect
import cats.data.ValidatedNel
import scala.meta._

import error.{SwaggyError, MultipleErrors}

private[swaggy] object syntax {

  implicit class MetaStringOps(val s: String) extends AnyVal {
    def term: Term.Name = Term.Name(s)
    def tpe: Type.Name = Type.Name(s)
    def listTpe: Type.Apply = Type.Apply("List".tpe, s.tpe :: Nil)
  }

  implicit class ValidatedNelOps[A](val validated: ValidatedNel[SwaggyError, A]) extends AnyVal {
    def orRaiseMultiple[F[_]: Effect]: F[A] =
      validated.fold(es => Effect[F].raiseError(MultipleErrors(es)), Effect[F].delay(_))
  }
}
