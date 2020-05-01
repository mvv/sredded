package com.github.mvv.sredded.impl

import java.time.temporal.{ChronoField, TemporalAccessor, TemporalField}

final class EpochMilliSecondAndNanos(epochSecond: Long, nanoseconds: Int) extends TemporalAccessor {
  override def isSupported(temporalField: TemporalField): Boolean =
    temporalField == ChronoField.INSTANT_SECONDS ||
      temporalField == ChronoField.MILLI_OF_SECOND ||
      temporalField == ChronoField.MICRO_OF_SECOND ||
      temporalField == ChronoField.NANO_OF_SECOND
  override def getLong(temporalField: TemporalField): Long =
    if (temporalField == ChronoField.INSTANT_SECONDS) {
      epochSecond
    } else if (temporalField == ChronoField.MILLI_OF_SECOND) {
      nanoseconds / 1000000
    } else if (temporalField == ChronoField.MICRO_OF_SECOND) {
      nanoseconds / 1000
    } else {
      nanoseconds
    }
}
