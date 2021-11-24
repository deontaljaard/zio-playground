package playground.zioplayground.country.http

import io.circe.Decoder
import io.circe.parser.decode
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import playground.zioplayground.country.domain.Country.decoder
import playground.zioplayground.country.domain.{CountryCode, CountryPatchForm, CountryPostForm}
import playground.zioplayground.country.repository.CountryRepository
import playground.zioplayground.utils.HTTPSpec.request
import zio._
import zio.interop.catz._
import zio.test.Assertion.equalTo
import zio.test._

object CountryServiceSpec extends DefaultRunnableSpec {
  type CountryTask[A] = RIO[CountryRepository, A]

  private val app = CountryService.routes[CountryRepository]().orNotFound

  implicit val countryPostFormEntityEncoder: EntityEncoder[CountryTask, CountryPostForm] = jsonEncoderOf[CountryTask, CountryPostForm]
  implicit val countryPatchFormEntityEncoder: EntityEncoder[CountryTask, CountryPatchForm] = jsonEncoderOf[CountryTask, CountryPatchForm]

  override def spec =
    suite("CountryService")(
      testM("should create countries") {
        val countryPostForm = CountryPostForm("test", "TST", "test", "test")
        val expectedBody = countryPostForm.toCountry
        val createRequest = request[CountryTask](Method.POST, "/").withEntity(countryPostForm)
        val ioResponse = app.run(createRequest)

        for {
          response <- ioResponse
          bodyAsString <- response.asStr
          country <- parseIO(bodyAsString)(decoder)
          bodyResult = assert(country)(equalTo(expectedBody))
          statusResult = assert(response.status)(equalTo(Status.Created))
        } yield statusResult && bodyResult
      },
      testM("should delete a country") {
        val countryPostForm = CountryPostForm("test", "TST", "test", "test")
        val createRequest = request[CountryTask](Method.POST, "/").withEntity(countryPostForm)
        val deleteRequest = (code: CountryCode) => request[CountryTask](Method.DELETE, s"/${code.code}")
        val getRequest = (code: CountryCode) => request[CountryTask](Method.GET, s"/${code.code}")

        val ioResponse = app.run(createRequest)
        for {
          response <- ioResponse
          bodyAsString <- response.asStr
          country <- parseIO(bodyAsString)(decoder)
          getResponse <- app.run(deleteRequest(country.countryCode)) *> app.run(getRequest(country.countryCode))
          statusResult = assert(getResponse.status)(equalTo(Status.NotFound))
        } yield statusResult
      },
      testM("should get a country") {
        val countryPostForm = CountryPostForm("test", "TST", "test", "test")
        val expectedBody = countryPostForm.toCountry
        val createRequest = request[CountryTask](Method.POST, "/").withEntity(countryPostForm)
        val getRequest = (code: String) => request[CountryTask](Method.GET, s"/$code")

        val ioResponse = app.run(createRequest) *> app.run(getRequest(countryPostForm.code))
        for {
          response <- ioResponse
          bodyAsString <- response.asStr
          country <- parseIO(bodyAsString)(decoder)
          bodyResult = assert(country)(equalTo(expectedBody))
          statusResult = assert(response.status)(equalTo(Status.Ok))
        } yield statusResult && bodyResult
      },
      testM("should patch a country") {
        val countryPostForm = CountryPostForm("test", "TST", "test", "test")
        val countryPatchForm = CountryPatchForm(Some("test_name"), Some("test_region"))
        val expectedBody = countryPostForm.toCountry.update(countryPatchForm)
        val createRequest = request[CountryTask](Method.POST, "/").withEntity(countryPostForm)
        val patchRequest = (code: String) => request[CountryTask](Method.PATCH, s"/$code").withEntity(countryPatchForm)

        val ioResponse = app.run(createRequest) *> app.run(patchRequest(countryPostForm.code))
        for {
          response <- ioResponse
          bodyAsString <- response.asStr
          country <- parseIO(bodyAsString)(decoder)
          bodyResult = assert(country)(equalTo(expectedBody))
          statusResult = assert(response.status)(equalTo(Status.Ok))
        } yield statusResult && bodyResult
      }
    ).provideSomeLayer[ZEnv](InMemoryCountryRepository.layer)

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
