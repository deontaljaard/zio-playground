package playground.zioplayground.health.http

import io.circe._
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import zio.{RIO, ZLayer}
import zio.interop.catz._

object HealthService {
  def routes[R](): HttpRoutes[RIO[R, *]] = {

    type HealthTask[A] = RIO[R, A]
    val dsl: Http4sDsl[HealthTask] = Http4sDsl[HealthTask]
    import dsl._

    implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[HealthTask, A] = jsonEncoderOf[HealthTask, A]
    implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[HealthTask, A] = jsonOf[HealthTask, A]

    HttpRoutes.of[HealthTask] {
      case GET -> Root =>
        Ok(HealthCheck("hostname", "version"))
    }

  }

  case class HealthCheck(hostname: String, version: String)

  object HealthCheck {
    implicit val encoder: Encoder[HealthCheck] = deriveEncoder
    implicit val decoder: Decoder[HealthCheck] = deriveDecoder
  }
}
