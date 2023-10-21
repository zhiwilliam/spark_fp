package org.wzhi.layers.data

trait DataContainer[A] extends Serializable {
  //def flatMap[U: K](f: A => IterableOnce[U]): DataContainer[K, U]
  def outputToConsole: Unit
}
