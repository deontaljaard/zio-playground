package playground.zioplayground.country.repository

import playground.zioplayground.country.domain.{Country, CountryCode, CountryPatchForm, CountryPostForm}
import zio._

object CountryRepository {

  trait Service {
    def getByCode(countryCode: CountryCode): UIO[Option[Country]]

    def create(countryPostForm: CountryPostForm): UIO[Country]

    def delete(countryCode: CountryCode): UIO[Boolean]

    def update(countryCode: CountryCode,
               countryPatchForm: CountryPatchForm): UIO[Option[Country]]
  }

  def getByCode(countryCode: CountryCode): URIO[CountryRepository, Option[Country]] =
    ZIO.accessM(_.get.getByCode(countryCode))

  def create(countryPostForm: CountryPostForm): URIO[CountryRepository, Country] =
    ZIO.accessM(_.get.create(countryPostForm))

  def delete(countryCode: CountryCode): URIO[CountryRepository, Boolean] =
    ZIO.accessM(_.get.delete(countryCode))

  def update(countryCode: CountryCode,
             countryPatchForm: CountryPatchForm): URIO[CountryRepository, Option[Country]] =
    ZIO.accessM(_.get.update(countryCode, countryPatchForm))

}
