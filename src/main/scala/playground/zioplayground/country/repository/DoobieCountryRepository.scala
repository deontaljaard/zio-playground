package playground.zioplayground.country.repository

import cats.implicits.toFunctorOps
import doobie._
import doobie.free.connection
import doobie.hikari._
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import playground.zioplayground.config.{DatabaseConfig, getDatabaseConfig}
import playground.zioplayground.country.domain
import playground.zioplayground.country.domain.{Country, CountryCode, CountryPatchForm, CountryPostForm}
import playground.zioplayground.country.repository.DoobieCountryRepository.SQL
import zio._
import zio.blocking.Blocking
import zio.interop.catz._
import zio.interop.catz.implicits._

final case class DoobieCountryRepository(xa: Transactor[Task])
  extends CountryRepository.Service {

  override def getByCode(countryCode: domain.CountryCode): UIO[Option[domain.Country]] =
    SQL.getByCode(countryCode)
      .option
      .transact(xa)
      .orDie

  override def create(countryPostForm: CountryPostForm): UIO[Country] = {
    val country = countryPostForm.toCountry
    SQL.create(country)
      .run
      .transact(xa)
      .orDie
      .map(_ => country)
  }

  override def delete(countryCode: domain.CountryCode): UIO[Boolean] =
    SQL.delete(countryCode)
      .run
      .transact(xa)
      .orDie
      .map(_ > 0)

  override def update(countryCode: CountryCode,
                      countryPatchForm: CountryPatchForm): UIO[Option[domain.Country]] =
    (for {
      oldCountry <- SQL.getByCode(countryCode).option
      newCountry = oldCountry.map(_.update(countryPatchForm))
      _ <- newCountry.fold(connection.unit)(country => SQL.update(country).run.void)
    } yield newCountry)
      .transact(xa)
      .orDie
}

object DoobieCountryRepository {

  def layer: ZLayer[Blocking with DatabaseConfig, Throwable, CountryRepository] = {
    def initDb(cfg: DatabaseConfig.Config): Task[Unit] =
      Task {
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .load()
          .migrate()
      }.unit

    def mkTransactor(cfg: DatabaseConfig.Config): ZManaged[Blocking, Throwable, HikariTransactor[Task]] = {
      ZIO.runtime[Blocking]
        .toManaged_
        .flatMap { implicit runtime =>
          val connectEc = runtime.platform.executor.asEC
          for {
            transactor <- HikariTransactor
              .newHikariTransactor[Task](
                cfg.driver,
                cfg.url,
                cfg.user,
                cfg.password,
                connectEc,
              ).toManagedZIO
          } yield transactor
        }
    }

    ZLayer.fromManaged {
      for {
        cfg <- getDatabaseConfig.toManaged_
        _ <- initDb(cfg).toManaged_
        transactor <- mkTransactor(cfg)
      } yield new DoobieCountryRepository(transactor)
    }
  }

  object SQL {

    def getByCode(countryCode: CountryCode): Query0[Country] =
      sql"""
        select * from country where code = ${countryCode.code}
         """.query[Country]

    def create(country: Country): Update0 =
      sql"""
          insert into country (
            name, code, continent, region
          ) values (
           ${country.name}, ${country.countryCode},
           ${country.continent}, ${country.region}
          )
         """.update

    def delete(countryCode: CountryCode): Update0 =
      sql"""
        delete from country where code = ${countryCode.code}
         """.update

    def update(country: Country): Update0 =
      sql"""
        update country set
          name = ${country.name},
          region = ${country.region}
        where code = ${country.countryCode}
        """.update

  }
}
