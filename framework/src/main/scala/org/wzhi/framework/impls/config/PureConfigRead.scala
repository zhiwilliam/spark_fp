package org.wzhi.framework.impls.config

import cats.effect.{IO, Sync}
import pureconfig._

import scala.reflect.ClassTag

object PureConfigRead {
  def readConfFromString[F[_]: Sync, T: ClassTag : ConfigReader](configStr: String): F[T] = {
    import pureconfig.module.catseffect.syntax._
    import com.typesafe.config.ConfigFactory
    val config = ConfigFactory.parseString(configStr)
    ConfigSource.fromConfig(config).loadF[F, T]()
  }
}
