package org.wzhi.layers.read

import org.wzhi.layers.data.DataContainer

trait ReadData[F[_], A]{
  def readData: F[DataContainer[A]]
}
