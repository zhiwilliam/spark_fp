package org.wzhi.layers.data
import scala.reflect.runtime.universe.TypeTag
trait DataContainer[A] extends Serializable {
  def flatMap[U <: Product: TypeTag](f: A => IterableOnce[U]): DataContainer[U]
  def map[U <: Product: TypeTag](f: A => U): DataContainer[U]
  def outputToConsole: Unit
}
