package org.wzhi.framework.dataSource

import cats.tagless._

@finalAlg
@autoFunctorK
@autoSemigroupalK
@autoProductNK
trait DataSource[F[_]] {
  def read[A]: F[A]
}