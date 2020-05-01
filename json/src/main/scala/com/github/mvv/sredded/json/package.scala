package com.github.mvv.sredded

import scala.annotation.tailrec

package object json {
  private def indexOfEscapeChar(str: String, start: Int): Int = {
    var i = start
    while (i < str.length) {
      val c = str(i)
      if (c == '\"' || c == '\\' || c <= 0x1F) {
        return i
      }
      i += 1
    }
    -1
  }

  @tailrec
  private def escapeAt(builder: java.lang.StringBuilder, str: String, at: Int): java.lang.StringBuilder = {
    str(at) match {
      case '\"' => builder.append("\\\"")
      case '\\' => builder.append("\\\\")
      case '\b' => builder.append("\\b")
      case '\f' => builder.append("\\f")
      case '\n' => builder.append("\\n")
      case '\r' => builder.append("\\r")
      case '\t' => builder.append("\\t")
      case c    => builder.append("\\u").append(f"${c.toInt}%04x")
    }
    val next = at + 1
    indexOfEscapeChar(str, next) match {
      case -1 => builder.append(str.substring(next))
      case i =>
        if (i > next) {
          builder.append(str.substring(next, i))
        }
        escapeAt(builder, str, i)
    }
  }

  private def appendStringTo(builder: java.lang.StringBuilder, str: String): java.lang.StringBuilder = {
    builder.append('"')
    indexOfEscapeChar(str, 0) match {
      case -1 => builder.append(str)
      case i =>
        if (i > 0) {
          builder.append(str.substring(0, i))
        }
        escapeAt(builder, str, i)
    }
    builder.append('"')
  }

  private val Alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray

  private def appendBase64(builder: java.lang.StringBuilder, b: Byte): java.lang.StringBuilder = {
    val d1 = (b >> 2) & 0x3F
    val d2 = (b << 4) & 0x3F
    builder.append(Alphabet(d1)).append(Alphabet(d2)).append('=').append('=')
  }

  private def appendBase64(builder: java.lang.StringBuilder, b1: Byte, b2: Byte): java.lang.StringBuilder = {
    val d1 = (b1 >> 2) & 0x3F
    val d2 = ((b1 << 4) & 0x3F) | ((b2 >> 4) & 0x0F)
    val d3 = (b2 << 2) & 0x3F
    builder.append(Alphabet(d1)).append(Alphabet(d2)).append(Alphabet(d3)).append('=')
  }

  private def appendBase64(builder: java.lang.StringBuilder, b1: Byte, b2: Byte, b3: Byte): java.lang.StringBuilder = {
    val d1 = (b1 >> 2) & 0x3F
    val d2 = ((b1 << 4) & 0x3F) | ((b2 >> 4) & 0x0F)
    val d3 = ((b2 << 2) & 0x3F) | ((b3 >> 6) & 0x03)
    val d4 = b3 & 0x3F
    builder.append(Alphabet(d1)).append(Alphabet(d2)).append(Alphabet(d3)).append(Alphabet(d4))
  }

  private def appendBinaryTo(builder: java.lang.StringBuilder, bin: StructValue.Binary): java.lang.StringBuilder = {
    builder.append('"')
    var leftOverByte1 = 0: Byte
    var leftOverByte2 = 0: Byte
    var numLeftOvers = 0
    bin.chunks.foreach { chunk =>
      val chunkSize = chunk.length
      if (chunkSize > 0) {
        if (chunkSize == 2) {
          if (numLeftOvers == 2) {
            appendBase64(builder, leftOverByte1, leftOverByte2, chunk(0))
            leftOverByte1 = chunk(1)
            numLeftOvers = 1
          } else if (numLeftOvers == 1) {
            appendBase64(builder, leftOverByte1, chunk(0), chunk(1))
            numLeftOvers = 0
          } else {
            leftOverByte1 = chunk(0)
            leftOverByte2 = chunk(1)
            numLeftOvers = 2
          }
        } else if (chunkSize == 1) {
          if (numLeftOvers == 2) {
            appendBase64(builder, leftOverByte1, leftOverByte2, chunk(0))
            numLeftOvers = 0
          } else if (numLeftOvers == 1) {
            leftOverByte2 = chunk(0)
            numLeftOvers = 2
          } else {
            leftOverByte1 = chunk(0)
            numLeftOvers = 1
          }
        } else {
          var i = if (numLeftOvers == 2) {
            appendBase64(builder, leftOverByte1, leftOverByte2, chunk(0))
            numLeftOvers = 0
            1
          } else if (numLeftOvers == 1) {
            appendBase64(builder, leftOverByte1, chunk(0), chunk(1))
            numLeftOvers = 0
            2
          } else {
            0
          }
          while (i < chunkSize) {
            if (i <= chunkSize - 3) {
              appendBase64(builder, chunk(i), chunk(i + 1), chunk(i + 2))
              i += 3
            } else if (i == chunkSize - 2) {
              leftOverByte1 = chunk(i)
              leftOverByte2 = chunk(i + 1)
              numLeftOvers = 2
              i += 2
            } else {
              leftOverByte1 = chunk(i)
              numLeftOvers = 1
              i += 1
            }
          }
        }
      }
    }
    if (numLeftOvers == 2) {
      appendBase64(builder, leftOverByte1, leftOverByte2)
    } else if (numLeftOvers == 1) {
      appendBase64(builder, leftOverByte1)
    }
    builder.append('"')
  }

  private def appendValueTo(builder: java.lang.StringBuilder, value: StructValue): java.lang.StringBuilder =
    value match {
      case StructValue.Null         => builder.append("null")
      case StructValue.True         => builder.append("true")
      case StructValue.False        => builder.append("false")
      case StructValue.Int8(value)  => builder.append(value)
      case StructValue.Int16(value) => builder.append(value)
      case StructValue.Int32(value) => builder.append(value)
      case StructValue.Int64(value) => builder.append(value)
      case StructValue.Float32(value) =>
        if (value.isNaN) {
          builder.append("\"NaN\"")
        } else if (value.isPosInfinity) {
          builder.append("\"Infinity\"")
        } else if (value.isNegInfinity) {
          builder.append("\"-Infinity\"")
        } else {
          builder.append(value)
        }
      case StructValue.Float64(value) =>
        if (value.isNaN) {
          builder.append("\"NaN\"")
        } else if (value.isPosInfinity) {
          builder.append("\"Infinity\"")
        } else if (value.isNegInfinity) {
          builder.append("\"-Infinity\"")
        } else {
          builder.append(value)
        }
      case int: StructValue.BigInt     => builder.append(int)
      case num: StructValue.BigNum     => builder.append(num)
      case ts: StructValue.Timestamp64 => builder.append('"').append(ts).append('"')
      case ts: StructValue.Timestamp   => builder.append('"').append(ts).append('"')
      case StructValue.String(value)   => appendStringTo(builder, value)
      case bin: StructValue.Binary     => appendBinaryTo(builder, bin)
      case StructValue.Sequence(values) =>
        builder.append('[')
        var first = true
        values.foreach { value =>
          if (first) {
            first = false
          } else {
            builder.append(',')
          }
          appendValueTo(builder, value)
        }
        builder.append(']')
      case StructValue.Mapping(entries) =>
        builder.append('{')
        var first = true
        entries.foreach {
          case (name, value) =>
            if (first) {
              first = false
            } else {
              builder.append(',')
            }
            appendStringTo(builder, name)
            appendValueTo(builder.append(':'), value)
        }
        builder.append('}')
    }

  def appendAsJsonTo[A: Structured](builder: java.lang.StringBuilder, value: A): java.lang.StringBuilder =
    appendValueTo(builder, Structured(value))

  implicit final class StructuredAsJsonString[A](val underlying: A) extends AnyVal {
    def asJsonString(implicit structured: Structured[A]): String =
      appendAsJsonTo(new java.lang.StringBuilder, underlying).toString
  }
}
