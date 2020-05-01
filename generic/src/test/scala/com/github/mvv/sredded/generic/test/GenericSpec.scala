package com.github.mvv.sredded.generic.test

import com.github.mvv.sredded.{StructValue, Structured, StructuredMapping}
import com.github.mvv.sredded.generic.deriveStructured
import org.specs2.mutable.Specification

class GenericSpec extends Specification {
  import GenericSpec._

  "generic" >> {
    "should derive proper StructuredMapping instances" >> {
      implicit lazy val subEnityStructured: StructuredMapping[TestSubEntity] = deriveStructured
      implicit val enityStructured: StructuredMapping[TestEntity] = deriveStructured
      val result = Structured {
        TestEntity(
          bool = true,
          str = "foofoo",
          sub = TestSubEntity(int = 1, subsub = None),
          seq = Seq(TestSubEntity(int = 2, subsub = Some(TestSubEntity(int = 3, subsub = None))),
                    TestSubEntity(int = 4, subsub = None))
        )
      }
      result mustEqual StructValue.Mapping(
        Iterable(
          "bool" -> StructValue.True,
          "str" -> StructValue.String("foofoo"),
          "sub" -> StructValue.Mapping(Iterable("int" -> StructValue.Int32(1), "subsub" -> StructValue.Null)),
          "seq" -> StructValue.Sequence(
            Iterable(
              StructValue.Mapping(
                Iterable("int" -> StructValue.Int32(2),
                         "subsub" -> StructValue
                           .Mapping(Iterable("int" -> StructValue.Int32(3), "subsub" -> StructValue.Null)))
              ),
              StructValue.Mapping(Iterable("int" -> StructValue.Int32(4), "subsub" -> StructValue.Null))
            )
          )
        )
      )
    }
  }
}

object GenericSpec {
  final case class TestSubEntity(int: Int, subsub: Option[TestSubEntity])
  final case class TestEntity(bool: Boolean, str: String, sub: TestSubEntity, seq: Seq[TestSubEntity])
}
