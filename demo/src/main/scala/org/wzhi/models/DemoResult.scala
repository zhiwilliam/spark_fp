package org.wzhi.models

case class DemoResult(
                validated: Boolean,
                error: List[String] = List.empty[String],
                value: Option[EnrichedTransaction] = None
                )
