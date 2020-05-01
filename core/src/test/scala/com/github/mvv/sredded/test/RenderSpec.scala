package com.github.mvv.sredded.test

import com.github.mvv.sredded.StructValue
import org.specs2.mutable.Specification

class RenderSpec extends Specification {
  "StructValue.toString" >> {
    "should render values properly" >> {
      StructValue
        .Mapping(
          Seq(
            "a\n[b=]" -> StructValue.String("<<\r\t\\>:>"),
            "foo" -> StructValue.Sequence(
              Seq(StructValue.Timestamp64(0L), StructValue.BigNum(BigInt(12300).bigInteger, BigInt(-8).bigInteger))
            )
          )
        )
        .toString mustEqual
        "{a\\n\\[b\\=\\]=\\<\\<\\r\\t\\\\\\>:\\>,foo=[1970-01-01T00:00:00Z,123E-6]}"
    }
  }
}
