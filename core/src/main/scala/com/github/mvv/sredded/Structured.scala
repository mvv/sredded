package com.github.mvv.sredded

import java.time.Instant
import java.util.Date

import scala.language.{higherKinds, implicitConversions}

trait Structured[A] {
  def apply(value: A): StructValue
}

trait StructuredLowest {
  implicit def iterableStructured[A: Structured, S[_]](implicit witness: S[A] <:< Iterable[A]): Structured[S[A]] = {
    values => StructValue.Sequence(witness(values).map(Structured(_)))
  }
}

trait StructuredLow extends StructuredLowest {
  implicit def mappingStructured[A: StructuredMapping]: Structured[A] = { value => StructuredMapping(value) }
}

object Structured extends StructuredLow {
  def apply[A: Structured](value: A): StructValue = implicitly[Structured[A]].apply(value)

  implicit val structValueStructured: Structured[StructValue] = { value => value }
  implicit val structValueSequenceStructured: Structured[StructValue.Sequence] = { value => value }
  implicit val structValueMappingStructured: Structured[StructValue.Mapping] = { value => value }
  implicit val booleanStructured: Structured[Boolean] = { value => StructValue.Boolean(value) }
  implicit val byteStructured: Structured[Byte] = { value => StructValue.Int8(value) }
  implicit val shortStructured: Structured[Short] = { value => StructValue.Int16(value) }
  implicit val intStructured: Structured[Int] = { value => StructValue.Int32(value) }
  implicit val longStructured: Structured[Long] = { value => StructValue.Int64(value) }
  implicit val dateStructured: Structured[Date] = { value => StructValue.Timestamp64(value.getTime) }
  implicit val instantStructured: Structured[Instant] = { value =>
    StructValue.Timestamp.unsafe(value.getEpochSecond, value.getNano)
  }
  implicit val floatStructured: Structured[Float] = { value => StructValue.Float32(value) }
  implicit val doubleStructured: Structured[Double] = { value => StructValue.Float64(value) }
  implicit val bigIntegerStructured: Structured[java.math.BigInteger] = { value => StructValue.BigInt(value) }
  implicit val bigIntStructured: Structured[BigInt] = { value => Structured(value.bigInteger) }
  implicit val bigDecimalStructured: Structured[java.math.BigDecimal] = { value =>
    StructValue.BigNum(value.unscaledValue, java.math.BigInteger.valueOf(value.scale))
  }
  implicit val bigDecStructured: Structured[BigDecimal] = { value => Structured(value.bigDecimal) }
  implicit val stringStructured: Structured[String] = { value => StructValue.String(value) }
  implicit val byteArrayStructured: Structured[Array[Byte]] = { value => StructValue.Binary(Iterable(value)) }
  implicit def optionStructured[A: Structured]: Structured[Option[A]] = {
    case Some(value) => Structured(value)
    case None        => StructValue.Null
  }
}
