package org.wzhi.impls.config

import cats.effect.IO
import org.wzhi.framework.impls.config.PureConfigRead
import org.wzhi.framework.impls.io.file.FileRead
import org.wzhi.models._

object DemoReadConfig {
  case class IOConfig(filePath: String) {
    import pureconfig.generic.auto._
    def read: IO[DemoConfig] =
      FileRead.makeResourceForRead(filePath).use(x =>
        PureConfigRead.readConfFromString[IO, DemoConfig](x.getLines().mkString("\n")))
  }
}
