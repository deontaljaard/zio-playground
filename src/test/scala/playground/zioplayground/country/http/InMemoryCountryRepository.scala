package playground.zioplayground.country.http
import playground.zioplayground.country.domain
import playground.zioplayground.country.domain.{Country, CountryCode}
import playground.zioplayground.country.repository.CountryRepository
import zio._

final private class InMemoryCountryRepository(ref: Ref[Map[CountryCode, Country]])
  extends CountryRepository.Service {

  override def getByCode(countryCode: CountryCode): UIO[Option[Country]] =
    ref.get.map(_.get(countryCode))

  override def create(countryPostForm: domain.CountryPostForm): UIO[Country] = {
    val country = countryPostForm.toCountry
    for {
      _ <- ref.update(countries => countries + (country.countryCode -> country))
    } yield country
  }

  override def delete(countryCode: CountryCode): UIO[Boolean] = ref.update(countries => countries - countryCode).map(_ => true)

  override def update(countryCode: CountryCode, countryPatchForm: domain.CountryPatchForm): UIO[Option[Country]] =
    for {
      oldCountry <- getByCode(countryCode)
      result <- oldCountry.fold[UIO[Option[Country]]](ZIO.succeed(None)) { country =>
        val newCountry = country.update(countryPatchForm)
        ref.update(countries => countries + (newCountry.countryCode -> newCountry)) *>
          ZIO.succeed(Some(newCountry))
      }
    } yield result
}

object InMemoryCountryRepository {

  val layer: ZLayer[Any, Nothing, CountryRepository] =
    ZLayer.fromEffect {
      for {
        ref     <- Ref.make(Map.empty[CountryCode, Country])
      } yield new InMemoryCountryRepository(ref)
    }

}
