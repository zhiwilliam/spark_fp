package org.wzhi.impls.data

import org.apache.spark.sql.{Dataset, Encoder, SparkSession}
import org.wzhi.layers.data.DataContainer

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.TypeTag

object DataContainerImpls {

  case class BatchDatasetContainer[A](data: Dataset[A])(implicit val spark: SparkSession) extends DataContainer[A] {
    override def outputToConsole: Unit = {
      data.collect.foreach(println)
    }

    override def flatMap[U <: Product: TypeTag](f: A => IterableOnce[U]): DataContainer[U] = {
      import spark.implicits._
      BatchDatasetContainer(data.flatMap(f))
    }

    override def map[U<: Product: TypeTag](f: A => U): DataContainer[U] = {
      import spark.implicits._
      BatchDatasetContainer(data.map(f))
    }
  }

  case class ListContainer[A](data: List[A]) extends DataContainer[A] {
    override def flatMap[U <: Product : universe.TypeTag](f: A => IterableOnce[U]): DataContainer[U] =
      ListContainer(data.flatMap(f))

    override def map[U <: Product : universe.TypeTag](f: A => U): DataContainer[U] =
      ListContainer(data.map(f))

    override def outputToConsole: Unit = data.foreach(println)
  }
}
