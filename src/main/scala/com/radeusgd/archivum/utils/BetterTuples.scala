package com.radeusgd.archivum.utils

object BetterTuples {
   implicit class BetterTuple[T, U](t: (T, U)) {
      def mapFirst[V](f: T => V): (V, U) = (f(t._1), t._2)
      def mapSecond[V](f: U => V): (T, V) = (t._1, f(t._2))
   }

   implicit class BetterTupleList[T, U](s: Seq[(T, U)]) {
      def mapFirst[V](f: T => V): Seq[(V, U)] = s.map(_.mapFirst(f))
      def mapSecond[V](f: U => V): Seq[(T, V)] = s.map(_.mapSecond(f))

      def extractFirsts: Seq[T] = s.map(_._1)
      def extractSeconds: Seq[U] = s.map(_._2)
   }
}
