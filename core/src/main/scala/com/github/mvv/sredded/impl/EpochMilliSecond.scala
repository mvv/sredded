package com.github.mvv.sredded.impl

import java.time.temporal.{ChronoField, TemporalAccessor, TemporalField}

final class EpochMilliSecond(value: Long) extends TemporalAccessor {
  override def isSupported(temporalField: TemporalField): Boolean =
    temporalField == ChronoField.INSTANT_SECONDS ||
      temporalField == ChronoField.MILLI_OF_SECOND ||
      temporalField == ChronoField.MICRO_OF_SECOND ||
      temporalField == ChronoField.NANO_OF_SECOND
  override def getLong(temporalField: TemporalField): Long = {
    val remainder = value % 1000L
    if (temporalField == ChronoField.INSTANT_SECONDS) {
      val seconds = value / 1000L
      if (remainder < 0L) {
        seconds - 1L
      } else {
        seconds
      }
    } else {
      val millis = if (remainder < 0L) {
        1000L + remainder
      } else {
        remainder
      }
      if (temporalField == ChronoField.MICRO_OF_SECOND) {
        millis * 1000L
      } else if (temporalField == ChronoField.NANO_OF_SECOND) {
        millis * 1000000L
      } else {
        millis
      }
    }
  }
}
