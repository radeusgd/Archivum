package com.radeusgd.archivum.querying

import cats.Monoid
import cats.kernel.Semigroup

case class ListMap[K, V] private (private val elems: List[(K, V)]) {
   def entries: List[(K, V)] = elems.reverse
   def values: List[V] = elems.map(_._2).reverse
   def keys: List[K] = elems.map(_._1).reverse

   def updated(k: K, v: V): ListMap[K, V] = new ListMap[K, V]((k, v) :: elems)

   def mapValues[U](f: V => U): ListMap[K, U] = new ListMap[K, U](
      elems.map((t) => (t._1, f(t._2)))
   )

   def append(other: ListMap[K, V]): ListMap[K, V] =
      ListMap.fromList(entries ++ other.entries)

   override def toString: String = entries.toString()
}

object ListMap {
   def empty[K, V]: ListMap[K, V] = new ListMap[K, V](Nil)
   def fromList[K, V](entries: List[(K, V)]): ListMap[K, V] = new ListMap[K, V](entries.reverse)
}

sealed abstract class NestedMapADT[K, +V] {
   def map[U](f: V => U): NestedMapADT[K, U]
   def flatMap1[U](f: V => NestedMapADT[K, U]): NestedMapADT[K, U]
   def flatMap[U](f: V => NestedMap[K, U]): NestedMap[K, U]
   def flatten: List[V]

   def asSingleton: NestedMapElement[K, V]
}

object NestedMapADT {
   def empty[K, V]: NestedMap[K, V] = NestedMap.empty
   def singleton[K, V](v: V): NestedMapElement[K, V] = NestedMapElement(v)

   def merge[K, V](a: NestedMapADT[K, V], b: NestedMapADT[K, V])
                  (implicit merger: Semigroup[V]): NestedMapADT[K, V] = {
      (a, b) match {
         case (NestedMapElement(va), NestedMapElement(vb)) => NestedMapElement(merger.combine(va, vb))
         case (NestedMap(ma), NestedMap(mb)) =>
            if (ma.keys != mb.keys) throw new RuntimeException("NestedMapADT.merge - incompatible shapes")
            else {
               val together = ma.entries.zip(mb.values)
               val merged = together.map({
                  case ((k, va), vb) => (k, merge(va, vb))
               })
               NestedMap(ListMap(merged))
            }
         case _ => throw new RuntimeException("NestedMapADT.merge - incompatible shapes")
      }
   }
}

case class NestedMapElement[K, +V](value: V) extends NestedMapADT[K,V] {
   def map[U](f: V => U): NestedMapElement[K, U] = NestedMapElement(f(value))

   override def flatMap1[U](f: V => NestedMapADT[K, U]): NestedMapADT[K, U] = f(value)

   override def flatMap[U](f: V => NestedMap[K, U]): NestedMap[K, U] = f(value)

   override def flatten: List[V] = value :: Nil

   override def toString: String = value.toString

   override def asSingleton: NestedMapElement[K, V] = this
}

case class NestedMap[K, V](mapping: ListMap[K, NestedMapADT[K, V]]) extends NestedMapADT[K,V] {
   // TODO iteration
   def map[U](f: V => U): NestedMap[K, U] =
      NestedMap(mapping.mapValues {
         case NestedMapElement(v) => NestedMapElement(f(v))
         case m: NestedMap[K, V] => m.map(f)
      })

   def updated(k: K, v: V): NestedMap[K, V] =
      NestedMap(mapping.updated(k, NestedMapElement(v)))

   def updated(k: K, m: NestedMap[K, V]): NestedMap[K, V] =
      NestedMap(mapping.updated(k, m))

   override def flatMap1[U](f: V => NestedMapADT[K, U]): NestedMap[K, U] =
      NestedMap(mapping.mapValues(_.flatMap1(f)))

   override def flatMap[U](f: V => NestedMap[K, U]): NestedMap[K, U] =
      NestedMap(mapping.mapValues(_.flatMap(f)))

   override def flatten: List[V] =
      mapping.values.flatMap {
         case NestedMapElement(v) => v :: Nil
         case m: NestedMap[K, V] => m.flatten
      }

   override def asSingleton: NestedMapElement[K, V] = throw new RuntimeException("Not a singleton NestedMap")

   def append(other: NestedMap[K, V]): NestedMap[K, V] =
      NestedMap(mapping.append(other.mapping))

   override def toString: String =
      "NestedMap(" + mapping.entries.map({ case (k, v) => k + "->" + v}).mkString(", ") + ")"
}

object NestedMap {
   def empty[K, V]: NestedMap[K, V] = NestedMap(ListMap.empty)
   def fromList[K, V](l: List[(K, V)]): NestedMap[K, V] =
      NestedMap(ListMap.fromList(
         l.map({case (key, value) => (key, NestedMapElement[K, V](value))})
      ))
}
