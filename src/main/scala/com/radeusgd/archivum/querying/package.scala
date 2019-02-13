package com.radeusgd.archivum

import java.util.Locale

import com.radeusgd.archivum.datamodel._

import scala.reflect.ClassTag

package object querying {

   @deprecated
   def extractKey(key: String, safe: Boolean = true): (DMValue => DMValue) = {
      (v: DMValue) => {
         try {
            val struct: DMStruct = v.asInstanceOf[DMStruct]
            struct.values.getOrElse(key, if (safe) DMNull else throw new NoSuchElementException(key))
         } catch {
            case e: ClassCastException => if (safe) DMNull else throw e
         }
      }
   }

   implicit class GetterHelper(val sc: StringContext) extends AnyVal {
      def path(args: Any*): DMValue => DMValue =
         DMUtils.makeGetter(sc.s(args:_*))

   }

   type ResultRow = NestedMap[String, DMValue]

   object ResultRow {
      def apply(values: (String, DMValue)*): ResultRow =
         NestedMap.fromList(values.toList)
   }

   implicit class DMValueHelper(val dmv: DMValue) extends AnyVal {
      def asType[T <: DMValue : ClassTag]: Option[T] =
         dmv match {
            case t: T =>
               Some(t)
            case _ => None
         }

      def taknieasBool: Option[Boolean] = dmv match {
         case DMString("Tak") => Some(true)
         case DMString("Nie") => Some(false)
         case _ => None
      }
   }

   implicit class ObjectsHelper(val objs: Seq[DMValue]) extends AnyVal {
      def getProp(path: String): Seq[DMValue] = objs.map(DMUtils.makeGetter(path))

      def onlyWithType[T <: DMValue : ClassTag]: Seq[T] = objs.flatMap(_.asType[T])
   }

   implicit class NestedMapOfSequences[K, T](val nm: NestedMapADT[K, Seq[T]]) extends AnyVal {
      def count: Int = nm match {
         case NestedMapElement(seq) => seq.length
         case NestedMap(mapping) => mapping.values.map(_.count).sum
      }
   }

   implicit class MapHelper[K, V](val map: Map[K, V]) extends AnyVal {
      def merged(o: Map[K, V]): Map[K, V] =
         o.toList.foldLeft(map){ case (m, (k, v)) => m.updated(k, v) }
   }

   def percentage(part: Int, whole: Int): DMValue =
      DMStruct(Map("part" -> DMInteger(part), "whole" -> DMInteger(whole))) // this is a special encoding which XLS exporter understands

   implicit class PredicateHelper[T](val pred: T => Boolean) extends AnyVal {
      //noinspection ScalaStyle
      def &&(p2: T => Boolean): T => Boolean =
         (t: T) => pred(t) && p2(t)
   }
}
