package playground.zioplayground.http

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.parser.decode
import org.http4s._
import org.http4s.circe.jsonOf
import org.http4s.implicits._
import playground.zioplayground.http.HTTPSpec.request
import playground.zioplayground.http.HealthService._
import zio._
import zio.interop.catz._
import zio.test.Assertion._
import zio.test._

object HealthServiceSpec extends DefaultRunnableSpec {

  type HealthTask[A] = RIO[Any, A]

  private val app = HealthService.routes[Any]().orNotFound

  implicit val decoder: Decoder[HealthCheck] = deriveDecoder

  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[HealthTask, HealthCheck] = jsonOf[HealthTask, HealthCheck]

  override def spec = {
    suite("HealthService")(
      testM("should return health check") {
        val req = request[HealthTask](Method.GET, "/")
        val expectedBody = HealthCheck("hostname", "version")

        val ioResponse = app.run(req)
        for {
          response <- ioResponse
          bodyAsString <- response.asStr
          healthCheck <- parseIO(bodyAsString)(decoder)
          bodyResult = assert(healthCheck)(equalTo(expectedBody))
          statusResult = assert(response.status)(equalTo(Status.Ok))
        } yield statusResult && bodyResult
      }
    )
  }

  implicit class ResponseConverter[R](response: Response[RIO[R, *]]) {
    def asStr: ZIO[R, Throwable, String] =
      response.body.compile.toVector.map(bytes => bytes.map(_.toChar).mkString)
  }

  def parseIO[T](s: String)(implicit decoder: Decoder[T]): Task[T] = {
    IO.effect {
      decode[T](s) match {
        case Left(e) => throw e
        case Right(a) => a
      }
    }
  }
}
