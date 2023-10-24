package org.wzhi.core.validate.nel.str

import ValidatorStrErr.strNelValidator._
import ValidatorStrErr.validate
import org.wzhi.core.validate.Validator

object PredefinedValidators {
  def notNull[A]: Validator[VNel, A] = validate[A](_ != null) otherwise "cannot be null."

  def notEmptyStr: Validator[VNel, String] = validate[String](_.nonEmpty) otherwise "cannot be empty."

  def strLengthLimit(length: Int): Validator[VNel, String] = validate[String](_.length <= length) otherwise s"length exceeds $length."

  def valueIn[A](patterns: A*): Validator[VNel, A] =
    validate[A](value => patterns.contains(value)) otherwise s"not in ${patterns.mkString(" ")}"

  def email: Validator[VNel, String] = {
    val matcher = """^[A-Za-z0-9+_.-]+@(.+)$""".r
    validate[String](matcher matches _) otherwise "not a valid email address"
  }
}
