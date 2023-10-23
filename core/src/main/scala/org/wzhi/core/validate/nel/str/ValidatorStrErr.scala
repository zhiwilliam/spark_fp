package org.wzhi.core.validate.nel.str

import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import org.wzhi.core.validate.nel.NelValidator
import org.wzhi.core.validate.{ErrorIndicator, Validator}

import scala.language.implicitConversions

object ValidatorStrErr {
  val strNelValidator: NelValidator[String] = new NelValidator[String] {}

  import strNelValidator._

  type StrNel[A] = VNel[A]

  implicit def mkValidator[A](func: A => Either[String, A]): Validator[VNel, A] =
    (a: A) => func(a) match {
      case Left(e) =>
        if(a == null) Invalid(NonEmptyList.of(s"null $e"))
        else Invalid(NonEmptyList.of(s"${a.toString} $e"))
      case Right(a) => Valid(a)
    }

  implicit class StringAsErrorIndicator[A](indicator: String) extends ErrorIndicator[A, String] {
    override def value: A => String = _ => indicator

    override def enrichError(a: A, err: String): String = s"${value(a)}/$err"
  }

  implicit class FunctionErrorIndicator[A](indicator: A => String) extends ErrorIndicator[A, String] {
    override def value: A => String = a => indicator(a)

    override def enrichError(a: A, err: String): String = s"${value(a)}/$err"
  }

  trait SetError[A] {
    def otherwise(error: String): Validator[VNel, A]
  }

  def validate[A](matchCondition: A => Boolean): SetError[A] =
    (error: String) => mkValidator((a: A) => {
      if (!matchCondition(a))
        Left(error)
      else
        Right(a)
    })
}
