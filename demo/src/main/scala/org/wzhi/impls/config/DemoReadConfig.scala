package org.wzhi.impls.config

import cats.effect.IO
import org.wzhi.framework.impls.config.PureConfigRead
import org.wzhi.framework.impls.io.file.FileRead
import pureconfig.ConfigReader

import scala.reflect.ClassTag

object DemoReadConfig {
  trait ReadFileWithIO[A] {
    def fromFile (filePath: String): IO[A]
  }

  def read[A: ClassTag : ConfigReader]: ReadFileWithIO[A] =
    (filePath: String) => FileRead.makeResourceForRead(filePath).use(x =>
      PureConfigRead.readConfFromString[IO, A](x.getLines().mkString("\n")))
}
