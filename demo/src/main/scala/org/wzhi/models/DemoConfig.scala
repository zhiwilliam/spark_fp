package org.wzhi.models

case class ReadCsvConfig(
                          dataFilePath: String
                        )
case class DemoConfig(readCsvConfig: ReadCsvConfig)
