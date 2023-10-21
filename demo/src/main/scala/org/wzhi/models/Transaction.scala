package org.wzhi.models

import java.sql.Timestamp

case class Transaction(id: String, account: String, time: Timestamp)
