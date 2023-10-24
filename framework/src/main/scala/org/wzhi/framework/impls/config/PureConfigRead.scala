package org.wzhi.framework.impls.config

import cats.effect.{IO, Sync}
import org.wzhi.framework.impls.io.file.FileRead
import pureconfig._

import scala.reflect.ClassTag

object PureConfigRead {
  def readConfFromString[F[_]: Sync, T: ClassTag : ConfigReader](configStr: String): F[T] = {
    import pureconfig.module.catseffect.syntax._
    import com.typesafe.config.ConfigFactory
    val config = ConfigFactory.parseString(configStr)
    ConfigSource.fromConfig(config).loadF[F, T]()
  }

  trait ReadFileWithIO[A] {
    def fromFile(filePath: String): IO[A]
  }

  // todo: This method only reads from classpath, we need a way to read from absolute file path.
  // Also, think about read from http or S3 bucket.

  def read[A: ClassTag : ConfigReader]: ReadFileWithIO[A] =
    (filePath: String) => FileRead.makeResourceForRead(filePath).use(x =>
      PureConfigRead.readConfFromString[IO, A](x.getLines().mkString("\n")))
}
