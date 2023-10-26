package org.wzhi.framework

import org.wzhi.framework.DataFlowImpls.BatchDatasetContainer
import scala.reflect.runtime.universe.TypeTag
object GroupByKeyImpls {
 /* implicit class SparkDataset[T](container: BatchDatasetContainer[T]) extends GroupByKey[BatchDatasetContainer, T] {
    override def groupByKey[K <: Product: TypeTag](func: T => K): Unit = {
      import container.spark.implicits._
      container.data.groupByKey(func)
    }
  }*/
}
