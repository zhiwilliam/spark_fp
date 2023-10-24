package org.wzhi.framework

import scala.reflect.runtime.universe.TypeTag
trait DataFlow[A] extends Serializable {
  def flatMap[U <: Product: TypeTag](f: A => IterableOnce[U]): DataFlow[U]
  def map[U <: Product: TypeTag](f: A => U): DataFlow[U]
  def outputToConsole: Unit
  def foreach[U](f: A => U): Unit

  def headOption: Option[A]
}

trait DataStatic[A] extends Serializable {
  def value: A
}

