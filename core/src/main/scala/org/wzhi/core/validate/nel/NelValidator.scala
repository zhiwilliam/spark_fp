package org.wzhi.core.validate.nel

import cats.ApplicativeError
import cats.data.Validated.{Invalid, Valid}
import cats.data._
import cats.implicits.catsSyntaxSemigroup
import org.wzhi.core.validate._
import org.wzhi.core.validate.Operators.Operators

trait NelValidator[E] {
  type VNel[A] = ValidatedNel[E, A]

  implicit val ae: ApplicativeError[VNel, E] = new ApplicativeError[VNel, E] {
    import cats.derived.auto.iterable.kittensMkIterable
    def pure[A](x: A): VNel[A] = Validated.valid(x)
    def handleErrorWith[A](fa: VNel[A])(f: E => VNel[A]): VNel[A] = fa match {
      case Invalid(e) =>
        val (valid, invalid) = e.map(f).partition(_.isInvalid)
        if (invalid.nonEmpty)
          Invalid(NonEmptyList.fromList(invalid.flatMap { case Invalid(e) => e }.toList).get)
        else valid.head
      case x => x
    }
    def raiseError[A](e: E): VNel[A] = Validated.Invalid(NonEmptyList.of(e))
    def ap[A, B](ff: VNel[A => B])(fa: VNel[A]): VNel[B] = fa ap ff
  }

  case class DataValidator[A](validatorList: List[(Operators, Validator[VNel, A])])
    extends ChainedValidator[VNel, A] {

    import cats.implicits._

    private lazy val validateList: List[(Operators, Validator[VNel, A])] = validatorList.reverse

    override def validate(a: A): VNel[A] = validateList.foldLeft(ae.pure(a)) {
      case (validated, (oper, validator)) => oper match {
        case Operators.>>+ => validated match {
          case Invalid(e) => Invalid(e)
          case Valid(_) => validator.validate(a) match {
            case Invalid(e) => Invalid(e)
            case _ => Valid(a)
          }
        }
        case Operators.<+> => (validated, validator.validate(a)) match {
          case (Invalid(e1), Invalid(e2)) => Invalid(e1 |+| e2)
          case (_, Invalid(e2)) => Invalid(e2)
          case (Invalid(e1), _) => Invalid(e1)
          case _ => Valid(a)
        }
        case Operators.<<+ => validated match {
          case Invalid(e1) => validator.validate(a) match {
            case Invalid(e2) => Invalid(e1 |+| e2)
            case _ => Valid(a)
          }
          case _ => Valid(a)
        }
      }
    }
  }

  case class FieldValidator[A, B]( errorIndicator: ErrorIndicator[A, E],
                                   method: A => B,
                                   validator: Validator[VNel, B]
                                 ) extends MethodValidator[VNel, A, B] {
    override def validate(a: A): VNel[A] = validator.validate(method(a)) match {
      case Invalid(e) => Invalid(e.map(errorIndicator.enrichError(a, _)))
      case Valid(_) => Valid(a)
    }
  }

  implicit class NelValidatorOps[A](validator: Validator[VNel, A]) extends ValidatorOps[VNel, A] {
    def lift[B](f: B => A, info: ErrorIndicator[B, E]): FieldValidator[B, A] = FieldValidator(info, f, validator)

    def forList(info: ErrorIndicator[A, E]): Validator[VNel, List[A]] = (a: List[A]) => {
      val nil: VNel[List[A]] = Validated.valid(List.empty[A])
      val namedValidator = validator.lift[A](x => x, info)
      a.map(namedValidator.validate).foldLeft(nil) {
        case (Valid(listA), next) => next match {
          case Valid(a) => Validated.valid(a :: listA)
          case Invalid(e) => Validated.invalid(e)
        }
        case (Invalid(e1), next) => next match {
          case Valid(_) => Invalid(e1)
          case Invalid(e2) => Invalid(e1 |+| e2)
        }
      }
    }

    def forOption: Validator[VNel, Option[A]] = {
      case Some(value) => validator.validate(value) match {
        case Valid(a) => Valid(Option(a))
        case Invalid(e) => Invalid(e)
      }
      case None => Valid(None)
    }


    override def >>+(other: Validator[VNel, A]): Validator[VNel, A] = validator match {
      case DataValidator(list) => DataValidator((Operators.>>+, other)::list)
      case v => DataValidator((Operators.>>+, other)::(Operators.<+>, v)::Nil)
    }

    override def <<+(other: Validator[VNel, A]): Validator[VNel, A] = validator match {
      case DataValidator(list) => DataValidator((Operators.<<+, other)::list)
      case v => DataValidator((Operators.<<+, other)::(Operators.<+>, v)::Nil)
    }

    override def <+>(other: Validator[VNel, A]): Validator[VNel, A] = validator match {
      case DataValidator(list) => DataValidator((Operators.<+>, other)::list)
      case v => DataValidator((Operators.<+>, other)::(Operators.<+>, v)::Nil)
    }
  }
}