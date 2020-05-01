package com.github.mvv.sredded.generic

import scala.reflect.macros.blackbox

object MacroUtils {
  final case class MethodParam[TermName, Type](paramName: TermName, paramType: Type)

  def methodParams(c: blackbox.Context)(
      classType: c.universe.Type,
      classSym: c.universe.ClassSymbol,
      methodSym: c.universe.MethodSymbol
  ): Seq[MethodParam[c.universe.TermName, c.universe.Type]] =
    methodSym.paramLists.flatten.map { paramSym =>
      MethodParam(paramSym.name.toTermName,
                  paramSym.typeSignature.substituteTypes(classSym.typeParams, classType.typeArgs))
    }

  def asCaseClass(c: blackbox.Context)(tpe: c.universe.Type): Option[c.universe.ClassSymbol] = {
    val typeSym = tpe.typeSymbol
    if (typeSym.isClass) {
      val classSym = typeSym.asClass
      if (classSym.isCaseClass) {
        Some(classSym)
      } else {
        None
      }
    } else {
      None
    }
  }

}
