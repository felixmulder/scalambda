package lambda

import java.io.{InputStream, OutputStream, ByteArrayOutputStream}
import java.nio.ByteBuffer
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}

import cats.effect.Effect
import io.circe.jawn.JawnParser
import io.circe.{Json, Printer}

abstract class JsonLambda[F[_]](implicit F: Effect[F])
  extends RequestStreamHandler {

  def process(json: Json): F[Json]

  private def read(input: InputStream): Array[Byte] = {
    val baos   = new ByteArrayOutputStream()
    val buffer = new Array[Byte](JsonLambda.BufSiz)
    var read   = input.read(buffer, 0, buffer.length)
    while (read != -1) {
      baos.write(buffer, 0, read)
      read = input.read(buffer, 0, buffer.length)
    }
    baos.flush()
    baos.toByteArray
  }

  override final def handleRequest(
    input: InputStream,
    output: OutputStream,
    ctx: Context
  ): Unit =
    JsonLambda
      .parser.parseByteBuffer(ByteBuffer.wrap(read(input)))
      .fold(
        throw _,
        json =>
          Effect[F]
            .toIO(process(json))
            .map(printResponse(_, output))
            .unsafeRunSync()
      )

  private def printResponse(json: Json, output: OutputStream): Unit =
    output.write(Printer.noSpaces.prettyByteBuffer(json).array)
}

object JsonLambda {
  protected[lambda] final val BufSiz =
    1024

  protected[lambda] final val parser: JawnParser =
    new JawnParser
}
