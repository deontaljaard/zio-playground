package playground.zioplayground

import playground.zioplayground.config.{AppConfig, DatabaseConfig, HttpConfig}
import playground.zioplayground.country.repository.{CountryRepository, DoobieCountryRepository}
import zio.ZLayer
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console

object layers {
  type BaseModules = Blocking with Clock with Console

  type Layer0Env =
    AppConfig with BaseModules

  type Layer1Env =
    Layer0Env with HttpConfig with DatabaseConfig

  type Layer2Env =
    Layer1Env with CountryRepository

  type AppEnv = Layer2Env

  object live {
    val layer0: ZLayer[BaseModules, Throwable, Layer0Env] =
      Blocking.any ++ Clock.any ++ Console.any ++ AppConfig.live

    val layer1: ZLayer[Layer0Env, Throwable, Layer1Env] =
      HttpConfig.fromAppConfig ++ DatabaseConfig.fromAppConfig ++ ZLayer.identity

    val layer2: ZLayer[Layer1Env, Throwable, Layer2Env] =
      DoobieCountryRepository.layer ++ ZLayer.identity

    val appLayer: ZLayer[BaseModules, Throwable, AppEnv] =
      layer0 >>> layer1 >>> layer2
  }
}
