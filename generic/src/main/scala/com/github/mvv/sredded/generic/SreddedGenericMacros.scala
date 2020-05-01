package com.github.mvv.sredded.generic

import com.github.mvv.sredded.StructuredMapping

import scala.reflect.macros.blackbox

class SreddedGenericMacros(val c: blackbox.Context) {
  import c.universe._

  private def extractEntries(classType: Type, classSym: ClassSymbol): Tree = {
    val params = MacroUtils.methodParams(c)(classType, classSym, classSym.primaryConstructor.asMethod)
    val paramsSize = params.size
    q"""
       new _root_.scala.collection.Iterable[(_root_.java.lang.String, _root_.com.github.mvv.sredded.StructValue)] {
         override def iterator: _root_.scala.collection.Iterator[
           (_root_.java.lang.String, _root_.com.github.mvv.sredded.StructValue)] =
           _root_.scala.collection.Iterator.tabulate($paramsSize) {
             case ..${params.zipWithIndex.map {
      case (param, i) =>
        cq"""$i => (${param.paramName.decodedName.toString},
                    implicitly[_root_.com.github.mvv.sredded.Structured[${param.paramType}]].apply(
                      value.${param.paramName}))"""
    }}
           }
         override def size: _root_.scala.Int = ${paramsSize}
       }
     """
  }

  def deriveStructured[A: WeakTypeTag]: Expr[StructuredMapping[A]] = {
    val tpe = weakTypeOf[A]
    MacroUtils.asCaseClass(c)(tpe) match {
      case Some(classSym) =>
        c.Expr[StructuredMapping[A]] {
          q"""
           new _root_.com.github.mvv.sredded.StructuredMapping[$tpe] {
             override def apply(value: $tpe): _root_.com.github.mvv.sredded.StructValue.Mapping =
               _root_.com.github.mvv.sredded.StructValue.Mapping(${extractEntries(tpe, classSym)})
           }
         """
        }
      case None =>
        c.abort(c.enclosingPosition, s"Type $tpe is not a case class")
    }
  }
}
