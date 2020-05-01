package com.github.mvv.sredded.impl

import java.time.format.DateTimeFormatter

import com.github.mvv.sredded.StructValue

import scala.annotation.tailrec

object RenderUtils {
  def toString(ts: StructValue.Timestamp64): String =
    DateTimeFormatter.ISO_INSTANT.format(new EpochMilliSecond(ts.epochMillisecond))

  def toString(ts: StructValue.Timestamp): String =
    DateTimeFormatter.ISO_INSTANT.format(new EpochMilliSecondAndNanos(ts.epochSecond, ts.nanoseconds))

  def toString(num: StructValue.BigNum): String = {
    val unscaled = num.unscaled.toString
    if (num.scale == java.math.BigInteger.ZERO) {
      unscaled
    } else {
      val trailingZeroes = unscaled.indices.reverse.takeWhile(unscaled(_) == '0').size
      if (trailingZeroes == 0) {
        s"${unscaled}E${num.scale}"
      } else if (trailingZeroes == 1 && unscaled.length == 1) {
        unscaled
      } else {
        val adjustedScale = num.scale.add(java.math.BigInteger.valueOf(trailingZeroes))
        s"${unscaled.substring(0, unscaled.length - trailingZeroes)}E$adjustedScale"
      }
    }
  }

  def toString(bin: StructValue.Binary): String = {
    val chunks = bin.chunks
    val builder = new java.lang.StringBuilder(chunks.iterator.map(_.length).sum.max(0))
    builder.append('<')
    chunks.foreach { chunk =>
      chunk.foreach { byte =>
        val digits = byte.toHexString
        if (digits.length == 1) {
          builder.append('0')
        }
        builder.append(digits)
      }
    }
    builder.append('>').toString
  }

  private val EscapeChars = "[]{}<>=, \b\f\r\n\t\\"

  private def indexOfEscapeChar(str: String, start: Int): Int = {
    var i = start
    while (i < str.length) {
      val c = str(i)
      if (EscapeChars.indexOf(c) >= 0) {
        return i
      }
      i += 1
    }
    -1
  }

  @tailrec
  private def escapeAt(builder: java.lang.StringBuilder, str: String, at: Int): String = {
    str(at) match {
      case '\b' => builder.append("\\b")
      case '\f' => builder.append("\\f")
      case '\r' => builder.append("\\r")
      case '\n' => builder.append("\\n")
      case '\t' => builder.append("\\t")
      case '\\' => builder.append("\\\\")
      case c    => builder.append('\\').append(c)
    }
    val next = at + 1
    indexOfEscapeChar(str, next) match {
      case -1 => builder.append(str.substring(next)).toString
      case i =>
        if (i > next) {
          builder.append(str.substring(next, i))
        }
        escapeAt(builder, str, i)
    }
  }

  def toString(str: String): String =
    indexOfEscapeChar(str, 0) match {
      case -1 => str
      case i =>
        val builder = new java.lang.StringBuilder(str.length + 1)
        if (i > 0) {
          builder.append(str.substring(0, i))
        }
        escapeAt(builder, str, i)
    }
}
