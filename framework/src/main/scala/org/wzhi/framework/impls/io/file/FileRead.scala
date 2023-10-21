package org.wzhi.framework.impls.io.file

import cats.effect._
import scala.io._

object FileRead {
  def makeResourceForRead(filePath: String): Resource[IO,Source] = {
    def sourceIO: IO[Source] = IO.blocking(Source.fromResource(filePath))
    Resource.make(sourceIO)(src => IO.blocking(src.close()))
  }
}
