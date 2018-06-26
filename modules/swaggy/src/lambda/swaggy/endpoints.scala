package lambda.swaggy

import scala.collection.JavaConverters._
import io.swagger.models.Swagger

object endpoints {

  final case class Endpoint(path: String)

  def apply(swagger: Swagger): List[Endpoint] = {
    swagger.getPaths().asScala.map { case (pathString, path) =>
      println("Path: " + pathString)
      path.getOperationMap.asScala.map { case (method, op) =>
        println(method.toString + " -- consumes:" + op.getConsumes + " params: " + op.getParameters)
      }
      println("-----------")
    }
    Nil
  }

}
