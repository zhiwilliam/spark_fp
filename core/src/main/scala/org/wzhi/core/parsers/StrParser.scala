package org.wzhi.core.parsers

import cats.data._

object StrParser {
  def parseBigDec(value: String): Validated[NumberFormatException, BigDecimal] =
    Validated.catchOnly[NumberFormatException](BigDecimal(value))

  def parseBoolean(value: String): Validated[IllegalArgumentException, Boolean] =
    Validated.catchOnly[IllegalArgumentException](value.toBoolean)
}
