package lambda.swaggy

import scala.meta.{Term, Type}

/** A lambda function that will handle an incoming request */
sealed trait MethodHandler {
  def name: String
  def path: String
}

/** A lambda function that will handle an incoming request with a body */
sealed trait MethodHandlerWithBody extends MethodHandler {
  def body: Type
}

final case class Get(name: String, path: String, queryParams: Map[Term, Type])
  extends MethodHandler

final case class Head(name: String, path: String, queryParams: Map[Term, Type])
  extends MethodHandler

final case class Post(name: String, path: String, body: Type)
  extends MethodHandlerWithBody

final case class Put(name: String, path: String, body: Type)
  extends MethodHandlerWithBody

final case class Delete(name: String, path: String)
  extends MethodHandler

final case class Options(name: String, path: String)
  extends MethodHandler

final case class Patch(name: String, path: String, body: Type)
  extends MethodHandlerWithBody

