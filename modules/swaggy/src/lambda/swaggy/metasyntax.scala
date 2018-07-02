package lambda.swaggy

import scala.meta._

object metasyntax {

  implicit class MetaStringOps(val s: String) extends AnyVal {
    def term: Term.Name = Term.Name(s)
    def tpe: Type.Name = Type.Name(s)
    def listTpe: Type.Apply = Type.Apply("List".tpe, s.tpe :: Nil)
  }


}
