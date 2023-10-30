package org.wzhi.framework.monads

import org.apache.spark.sql.{Dataset, SparkSession}

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.TypeTag

// A tasty of tagless final. But feel not as easier usage as mixed traits.
trait FlatMapF[K[_]] {
  def flatMap[A, U <: Product: TypeTag](fa: K[A])(f: A => IterableOnce[U]): K[U]

  def map[A, U<: Product: TypeTag](fa: K[A])(f: A => U): K[U]

}

object FlatMapF {
  class DatasetFlatMap[A](implicit spark: SparkSession) extends FlatMapF[Dataset] {
    override def flatMap[A, U <: Product : universe.TypeTag](fa: Dataset[A])(f: A => IterableOnce[U]): Dataset[U] = {
      import spark.implicits._
      fa.flatMap(f)
    }

    override def map[A, U <: Product : universe.TypeTag](fa: Dataset[A])(f: A => U): Dataset[U] = {
      import spark.implicits._
      fa.map(f)
    }
  }
}

