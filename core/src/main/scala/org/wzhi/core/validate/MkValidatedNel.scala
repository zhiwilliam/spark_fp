package org.wzhi.core.validate

import cats.data.Validated.{Invalid, Valid}
import cats.data._
import io.scalaland.chimney.partial.Error
import io.scalaland.chimney.partial.ErrorMessage.StringMessage

object MkValidatedNel {
  implicit class NumberFormatExp2ValidatedNel[A](data:Validated[NumberFormatException, A]) {
    def toNel(errorName: String): ValidatedNel[Error, A] =
      data.leftMap(e => NonEmptyList.of(Error(StringMessage(s"$errorName: ${e.getMessage}"))))
  }

  implicit class IllegalArgExp2ValidatedNel[A](data: Validated[IllegalArgumentException, A]) {
    def toNel(errorName: String): ValidatedNel[Error, A] =
      data.leftMap(e => NonEmptyList.of(Error(StringMessage(s"$errorName: ${e.getMessage}"))))
  }

  implicit class None2ValidatedNel[A](data: Option[A]) {
    def toNel(errorName: String): ValidatedNel[Error, A] =
      data.map(Valid(_)).getOrElse(Invalid(NonEmptyList.of(Error(StringMessage(s"$errorName has no value")))))
  }

}
