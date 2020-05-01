package com.github.mvv.sredded

import java.util.Objects

import com.github.mvv.sredded.impl.RenderUtils

sealed trait StructValue

object StructValue {
  sealed trait Scalar extends StructValue
  case object Null extends Scalar {
    override def toString: Predef.String = "null"
  }
  sealed trait Boolean extends Scalar {
    def value: scala.Boolean
  }
  case object True extends Boolean {
    override def value: scala.Boolean = true
    override def toString: Predef.String = "true"
  }
  case object False extends Boolean {
    override def value: scala.Boolean = false
    override def toString: Predef.String = "false"
  }
  object Boolean {
    def apply(value: scala.Boolean): Boolean = if (value) True else False
    def unapply(value: Boolean): Option[scala.Boolean] = Some(value.value)
  }
  final case class Int8(value: scala.Byte) extends Scalar {
    override def toString: Predef.String = value.toString
  }
  final case class Int16(value: scala.Short) extends Scalar {
    override def toString: Predef.String = value.toString
  }
  final case class Int32(value: scala.Int) extends Scalar {
    override def toString: Predef.String = value.toString
  }
  final case class Int64(value: scala.Long) extends Scalar {
    override def toString: Predef.String = value.toString
  }
  final case class Timestamp64(epochMillisecond: Long) extends Scalar {
    override def toString: Predef.String = RenderUtils.toString(this)
  }
  final class Timestamp private (val epochSecond: Long, val nanoseconds: Int) extends Scalar {
    override def hashCode: Int = Objects.hash(epochSecond: java.lang.Long, nanoseconds: java.lang.Integer)
    override def equals(that: Any): scala.Boolean =
      that match {
        case t: Timestamp => epochSecond == t.epochSecond && nanoseconds == t.nanoseconds
        case _            => false
      }
    override def toString: Predef.String = RenderUtils.toString(this)
  }
  object Timestamp {
    def apply(epochSecond: Long): Timestamp = new Timestamp(epochSecond, 0)
    def apply(epochSecond: Long, nanoseconds: Int): Option[Timestamp] =
      if (nanoseconds < 0 || nanoseconds >= 1000000000) {
        None
      } else {
        Some(new Timestamp(epochSecond, nanoseconds))
      }
    def unsafe(epochSecond: Long, nanoseconds: Int): Timestamp =
      Timestamp(epochSecond, nanoseconds).getOrElse {
        throw new IllegalArgumentException(s"Nanoseconds $nanoseconds are out of range")
      }
  }
  final case class Float32(value: scala.Float) extends Scalar {
    override def toString: Predef.String = value.toString
  }
  final case class Float64(value: scala.Double) extends Scalar {
    override def toString: Predef.String = value.toString
  }
  final case class BigInt(value: java.math.BigInteger) extends Scalar {
    override def toString: Predef.String = value.toString
  }
  final case class BigNum(unscaled: java.math.BigInteger, scale: java.math.BigInteger) extends Scalar {
    override def toString: Predef.String = RenderUtils.toString(this)
  }
  final case class String(value: java.lang.String) extends Scalar {
    override def toString: Predef.String = RenderUtils.toString(value)
  }
  final case class Binary(chunks: Iterable[Array[Byte]]) extends Scalar {
    override def hashCode: scala.Int = chunks.iterator.flatMap(_.iterator).foldLeft(1)(_ * 31 + _)
    override def equals(that: Any): scala.Boolean =
      that match {
        case binary: Binary =>
          chunks.iterator.flatMap(_.iterator).sameElements(binary.chunks.iterator.flatMap(_.iterator))
        case _ => false
      }
    override def toString: Predef.String = RenderUtils.toString(this)
  }
  final case class Sequence(values: Iterable[StructValue]) extends StructValue {
    override def hashCode: scala.Int = values.iterator.map(_.hashCode).foldLeft(1)(_ * 31 + _)
    override def equals(that: Any): scala.Boolean =
      that match {
        case seq: Sequence => values.iterator.sameElements(seq.values.iterator)
        case _             => false
      }
    override def toString: Predef.String = values.mkString("[", ",", "]")
  }
  final case class Mapping(entries: Iterable[(java.lang.String, StructValue)]) extends StructValue {
    override def hashCode: scala.Int = entries.iterator.map(_.hashCode).foldLeft(1)(_ * 31 + _)
    override def equals(that: Any): scala.Boolean =
      that match {
        case map: Mapping => entries.iterator.sameElements(map.entries.iterator)
        case _            => false
      }
    override def toString: Predef.String =
      entries.iterator.map {
        case (name, value) =>
          s"${RenderUtils.toString(name)}=$value"
      }.mkString("{", ",", "}")
  }
}
