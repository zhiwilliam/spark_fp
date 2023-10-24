package org.wzhi.framework

import org.apache.spark.broadcast.Broadcast
import io.scalaland.chimney.partial
import org.apache.spark.sql.{Dataset, SparkSession}

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.TypeTag

object DataFlowImpls {
  case class BatchDatasetContainer[A](data: Dataset[A])(implicit val spark: SparkSession) extends DataFlow[A] {
    override def outputToConsole: Unit = {
      data.collect.foreach(println)
    }

    override def flatMap[U <: Product: TypeTag](f: A => IterableOnce[U]): DataFlow[U] = {
      import spark.implicits._
      BatchDatasetContainer(data.flatMap(f))
    }

    override def map[U<: Product: TypeTag](f: A => U): DataFlow[U] = {
      import spark.implicits._
      BatchDatasetContainer(data.map(f))
    }

    override def foreach[U](f: A => U): Unit = {
      def unitForeach(a: A): Unit = f(a)
      data.foreach(unitForeach)
    }

    override def headOption: Option[A] = data.take(1).headOption
  }

  case class BroadCastStatic[A](data: Broadcast[A]) extends DataStatic[A] {
    def value: A = data.value
  }

  case class StreamDatasetContainer[A](data: Dataset[A])(implicit val spark: SparkSession) extends DataFlow[A] {
    override def outputToConsole: Unit = {
      data.writeStream
        .format("console")
        .option("checkpointLocation", "/tmp/_checkpoint")
        .outputMode("append")
        .start()
        .awaitTermination() // todo: Create a F: NeedWait def awaitTermination and delete this line.
    }

    override def flatMap[U <: Product : TypeTag](f: A => IterableOnce[U]): DataFlow[U] = {
      import spark.implicits._
      BatchDatasetContainer(data.flatMap(f))
    }

    override def map[U <: Product : TypeTag](f: A => U): DataFlow[U] = {
      import spark.implicits._
      StreamDatasetContainer(data.map(f))
    }

    override def foreach[U](f: A => U): Unit = {
      def unitForeach(a: A): Unit = f(a)

      data.foreach(unitForeach)
    }

    override def headOption: Option[A] = data.take(1).headOption
  }

  case class ListContainer[A](data: List[A]) extends DataFlow[A] {
    override def flatMap[U <: Product : universe.TypeTag](f: A => IterableOnce[U]): DataFlow[U] =
      ListContainer(data.flatMap(f))

    override def map[U <: Product : universe.TypeTag](f: A => U): DataFlow[U] =
      ListContainer(data.map(f))

    override def outputToConsole: Unit = data.foreach(println)

    override def foreach[U](f: A => U): Unit = data.foreach(f)

    override def headOption: Option[A] = data.headOption
  }

  case class ListStatic[A](data: A) extends DataStatic[A] {
    def value: A = data
  }
}
