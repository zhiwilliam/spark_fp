package org.wzhi.core.validate

import org.wzhi.core.validate.Operators.Operators

trait Validator[F[_], A] {
  def validate(a: A): F[A]
}

trait ChainedValidator[F[_], A] extends Validator[F, A]{
  def validatorList: List[(Operators, Validator[F, A])]
}

trait MethodValidator[F[_], A, B] extends Validator[F, A] {
  def method: A => B
  def validator: Validator[F, B]
}

// Defined all additional operations of a validator. So that we can assemble them to create stronger validator.
trait ValidatorOps[F[_], A] {
  // Find any invalid then return invalid immediately
  def >>+ (other: Validator[F, A]): Validator[F, A]
  // If any validator proved valid, then return valid
  def <<+ (other: Validator[F, A]): Validator[F, A]
  // Find all invalid items and return them back together.
  def <+> (nextValidator: Validator[F, A]): Validator[F, A]
}

trait ErrorIndicator[A, E] {
  def value: A => String
  def enrichError(a: A, err: E): E
}