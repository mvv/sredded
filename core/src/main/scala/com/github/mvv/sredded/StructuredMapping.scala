package com.github.mvv.sredded

import scala.language.{higherKinds, implicitConversions}

trait StructuredMapping[A] {
  def apply(value: A): StructValue.Mapping
}

object StructuredMapping {
  def apply[A: StructuredMapping](value: A): StructValue.Mapping = implicitly[StructuredMapping[A]].apply(value)

  implicit def mapStructuredMapping[A: Structured, M[_, _]](
      implicit witness: M[String, A] <:< Iterable[(String, A)]
  ): StructuredMapping[M[String, A]] = { map =>
    StructValue.Mapping(witness(map).map {
      case (key, value) => key -> Structured(value)
    })
  }
  implicit def entriesStructuredMapping[A: Structured, S[_]](
      implicit witness: S[A] <:< Iterable[(String, A)]
  ): StructuredMapping[S[A]] = { entries =>
    StructValue.Mapping(witness(entries).map {
      case (key, value) => key -> Structured(value)
    })
  }
}
