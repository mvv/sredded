package com.github.mvv.sredded.json.test

import java.nio.charset.StandardCharsets
import java.util.Base64

import com.github.mvv.sredded.StructValue
import com.github.mvv.sredded.json._
import org.specs2.mutable.Specification

class JsonSpec extends Specification {
  "JSON rendered" >> {
    "should render structured values to a proper JSON" >> {
      val chunks = Seq("a", "bc", "def", "ghij", "klmnopqrstuv", "\u00FF\u00FF\u00FF", "xyz!")
      val binaryChunks = chunks.map(_.getBytes(StandardCharsets.ISO_8859_1))
      StructValue
        .Mapping(
          Seq(
            "fo\to" -> StructValue.Float32(Float.NaN),
            "bar" -> StructValue.Sequence(
              Seq(
                StructValue.True,
                StructValue.Mapping(Seq("baz" -> StructValue.Binary(binaryChunks)))
              )
            )
          )
        )
        .asJsonString mustEqual
        s"""{"fo\\to":"NaN","bar":[true,{"baz":"${Base64.getEncoder.encodeToString(binaryChunks.flatten.toArray)}"}]}"""
    }
  }
}
