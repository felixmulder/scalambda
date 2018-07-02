package lambda.swaggy

import scala.meta.{Term, Type}

/** A lambda function that will handle an incoming request */
sealed trait Handler {
  def name: String
  def path: String
}

final case class Get(name: String, path: String, queryParams: Map[Term, Type])
  extends Handler

final case class Head(name: String, path: String, queryParams: Map[Term, Type])
  extends Handler

final case class Post(name: String, path: String, body: Type)
  extends Handler

final case class Put(name: String, path: String, body: Type)
  extends Handler

final case class Delete(name: String, path: String)
  extends Handler

final case class Options(name: String, path: String)
  extends Handler

final case class Patch(name: String, path: String, body: Type)
  extends Handler

