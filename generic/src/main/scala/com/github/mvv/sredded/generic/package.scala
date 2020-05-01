package com.github.mvv.sredded

import scala.language.experimental.macros

package object generic {
  def deriveStructured[A]: StructuredMapping[A] = macro SreddedGenericMacros.deriveStructured[A]
}
