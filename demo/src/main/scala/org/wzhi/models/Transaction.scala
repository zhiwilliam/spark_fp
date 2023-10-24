package org.wzhi.models

import java.sql.Timestamp

case class Money(amount: BigDecimal, currency: String)
case class Transaction(id: String, account: String, amount: String, country: String, time: Timestamp, isTest: String)

case class EnrichedTransaction(id: String, account: String, amount: Money, time: Timestamp, isTest: Boolean)
