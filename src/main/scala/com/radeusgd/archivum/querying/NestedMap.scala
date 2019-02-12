package com.radeusgd.archivum.querying

class ListMap[K, V] private (private val elems: List[(K, V)]) {
   def entries: List[(K, V)] = elems.reverse
   def values: List[V] = elems.map(_._2).reverse
   def keys: List[K] = elems.map(_._1).reverse

   def updated(k: K, v: V): ListMap[K, V] = new ListMap[K, V]((k, v) :: elems)

   def mapValues[U](f: V => U): ListMap[K, U] = new ListMap[K, U](
      elems.map((t) => (t._1, f(t._2)))
   )
}

object ListMap {
   def empty[K, V]: ListMap[K, V] = new ListMap[K, V](Nil)
   def fromList[K, V](entries: List[(K, V)]): ListMap[K, V] = new ListMap[K, V](entries.reverse)
}

sealed abstract class NestedMapADT[K, +V] {
   def map[U](f: V => U): NestedMapADT[K, U]
   def flatMap[U](f: V => NestedMapADT[K, U]): NestedMapADT[K, U]
}

object NestedMapADT {
   def empty[K, V]: NestedMap[K, V] = NestedMap.empty
   def singleton[K, V](v: V): NestedMapElement[K, V] = NestedMapElement(v)
}

case class NestedMapElement[K, +V](value: V) extends NestedMapADT[K,V] {
   def map[U](f: V => U): NestedMapElement[K, U] = NestedMapElement(f(value))

   override def flatMap[U](f: V => NestedMapADT[K, U]): NestedMapADT[K, U] = f(value)
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

   override def flatMap[U](f: V => NestedMapADT[K, U]): NestedMap[K, U] =
      NestedMap(mapping.mapValues(_.flatMap(f)))

   def flatten: List[V] =
      mapping.values.flatMap {
         case NestedMapElement(v) => v :: Nil
         case m: NestedMap[K, V] => m.flatten
      }
}

object NestedMap {
   def empty[K, V]: NestedMap[K, V] = NestedMap(ListMap.empty)
}
