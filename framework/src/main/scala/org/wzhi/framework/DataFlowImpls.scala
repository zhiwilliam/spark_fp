package org.wzhi.framework

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.{DataFrame, Dataset, KeyValueGroupedDataset, SparkSession}

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.TypeTag

object DataFlowImpls {
  case class BatchDatasetContainer[A](value: Dataset[A])(
    implicit val spark: SparkSession) extends DataFlow[A] with JoinFlow[BatchDatasetContainer, A] {
    override def outputToConsole: Unit = {
      value.collect.foreach(println)
    }

    override def flatMap[U <: Product: TypeTag](f: A => IterableOnce[U]): DataFlow[U] = {
      import spark.implicits._
      BatchDatasetContainer(value.flatMap(f))
    }

    override def map[U<: Product: TypeTag](f: A => U): DataFlow[U] = {
      import spark.implicits._
      BatchDatasetContainer(value.map(f))
    }

    override def foreach[U](f: A => U): Unit = {
      def unitForeach(a: A): Unit = f(a)
      value.foreach(unitForeach)
    }

    override def headOption: Option[A] = value.take(1).headOption

    /*override def groupByKey[K <: Product : universe.TypeTag](func: A => K): KeyValueGroupedDatasetContainer[A, K] = {
      import spark.implicits._
      KeyValueGroupedDatasetContainer(data.groupByKey(func))*/

    override def join[U](right: BatchDatasetContainer[U], joinExprs: String, joinType: String): FrameData = {
      val bb = value("sdfs") === right.value("dfsdf")
      val cc = value("sdfs") === right.value("dfsdf")
      val dd = bb && cc
      BatchSparkFrame(value.join(right.value))
    }
  }

  case class BatchSparkFrame(value: DataFrame)(implicit spark: SparkSession) extends FrameData {
    override def as[A <: Product : universe.TypeTag]: DataFlow[A] = {
      import spark.implicits._
      BatchDatasetContainer(value.as[A])
    }
  }

  case class KeyValueGroupedDatasetContainer[K, V](data: KeyValueGroupedDataset[K, V])(
    implicit val spark: SparkSession) {
  }

  case class BroadCastStatic[A](data: Broadcast[A]) extends DataStatic[A] {
    def value: A = data.value
  }

  case class StreamDatasetContainer[A](value: Dataset[A])(implicit val spark: SparkSession) extends DataFlow[A] {
    override def outputToConsole: Unit = {
      value.writeStream
        .format("console")
        .option("checkpointLocation", "/tmp/_checkpoint")
        .outputMode("append")
        .start()
        .awaitTermination() // todo: Create a F: NeedWait def awaitTermination and delete this line.
    }

    override def flatMap[U <: Product : TypeTag](f: A => IterableOnce[U]): DataFlow[U] = {
      import spark.implicits._
      BatchDatasetContainer(value.flatMap(f))
    }

    override def map[U <: Product : TypeTag](f: A => U): DataFlow[U] = {
      import spark.implicits._
      StreamDatasetContainer(value.map(f))
    }

    override def foreach[U](f: A => U): Unit = {
      def unitForeach(a: A): Unit = f(a)

      value.foreach(unitForeach)
    }

    override def headOption: Option[A] = value.take(1).headOption
  }

  case class ListContainer[A](value: List[A]) extends DataFlow[A] {
    override def flatMap[U <: Product : universe.TypeTag](f: A => IterableOnce[U]): DataFlow[U] =
      ListContainer(value.flatMap(f))

    override def map[U <: Product : universe.TypeTag](f: A => U): DataFlow[U] =
      ListContainer(value.map(f))

    override def outputToConsole: Unit = value.foreach(println)

    override def foreach[U](f: A => U): Unit = value.foreach(f)

    override def headOption: Option[A] = value.headOption
  }

  case class ListStatic[A](data: A) extends DataStatic[A] {
    def value: A = data
  }
}
