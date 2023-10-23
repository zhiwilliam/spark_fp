package org.wzhi.core.chimney_helpers

import cats.data.ValidatedNel
import io.scalaland.chimney.partial.Error

object Compatiable {
  implicit class SparkNormalize[A](data: ValidatedNel[Error, A]) {
    def normalize: ValidatedNel[String, A] =
      data.leftMap(x => x.map(e => e.message.asString))
  }
}
