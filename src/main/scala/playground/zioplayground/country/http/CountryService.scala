package playground.zioplayground.country.http

import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import playground.zioplayground.country.domain.{CountryCode, CountryPatchForm, CountryPostForm}
import playground.zioplayground.country.repository.CountryRepository
import zio._
import zio.interop.catz._

object CountryService {

  def routes[R <: CountryRepository](): HttpRoutes[RIO[R, *]] = {
    type CountryTask[A] = RIO[R, A]

    val dsl: Http4sDsl[CountryTask] = Http4sDsl[CountryTask]
    import dsl._

    implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[CountryTask, A] = jsonOf[CountryTask, A]

    implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[CountryTask, A] = jsonEncoderOf[CountryTask, A]

    HttpRoutes.of[CountryTask] {
      case GET -> Root / code =>
        for {
          maybeCountry <- CountryRepository.getByCode(CountryCode(code))
          response     <- maybeCountry.fold(NotFound())(Ok(_))
        } yield response

      case req @ POST -> Root =>
        req.decode[CountryPostForm] { country =>
         CountryRepository
            .create(country)
            .flatMap(Created(_))
        }

      case DELETE -> Root / code =>
        for {
          maybeCountry <- CountryRepository.getByCode(CountryCode(code))
          result       <- maybeCountry
            .map(x => CountryRepository.delete(CountryCode(code)))
            .fold(NotFound())(_.flatMap(Ok(_)))
        } yield result

      case req @ PATCH -> Root / code =>
        req.decode[CountryPatchForm] { updateForm =>
          for {
            update <- CountryRepository.update(CountryCode(code), updateForm)
            response <- update.fold(NotFound())(Ok(_))
          } yield response
        }
    }
  }

}
