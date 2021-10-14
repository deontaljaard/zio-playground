package playground.zioplayground

import playground.zioplayground.config.{AppConfig, HttpConfig}
import zio.ZLayer
import zio.blocking.Blocking
import zio.clock.Clock

object layers {
  type BaseModules = Blocking with Clock

  type Layer0Env =
    AppConfig with BaseModules

  type Layer1Env =
    Layer0Env with HttpConfig

  type Layer2Env =
    Layer1Env

  type AppEnv = Layer2Env

  object live {
    val layer0: ZLayer[BaseModules, Throwable, Layer0Env] =
      Blocking.any ++ Clock.any ++ AppConfig.live

    val layer1: ZLayer[Layer0Env, Throwable, Layer1Env] =
      HttpConfig.fromAppConfig ++ ZLayer.identity

    val layer2: ZLayer[Layer1Env, Throwable, Layer2Env] =
      ZLayer.identity

    val appLayer: ZLayer[BaseModules, Throwable, AppEnv] =
      layer0 >>> layer1 >>> layer2
  }
}
