package com.radeusgd.archivum.conversion

sealed abstract class Structure
case object Leaf extends Structure
case class Struct(fields: Map[String, Structure]) extends Structure
case class Array(inside: Structure) extends Structure

object Structure {
   type Path = List[String]
   def merge(a: Structure, b: Structure): Structure = (a, b) match {
      case (Leaf, x) => x
      case (x, Leaf) => x
      case (Array(ia), Array(ib)) => Array(merge(ia, ib))
      case (Struct(fa), Struct(fb)) =>
         val ka = fa.keySet
         val kb = fb.keySet
         val common = ka & kb
         val onlyA = ka &~ common
         val onlyB = kb &~ common
         val commons = common.toSeq.map(key => (key, merge(fa(key), fb(key))))
         val oas = onlyA.toSeq.map(key => (key, fa(key)))
         val obs = onlyB.toSeq.map(key => (key, fb(key)))
         Struct(Map(commons ++ oas ++ obs:_*))
      case (_, _) => throw new RuntimeException("Cannot merge structures of different shapes")
   }

   case class Differences(missing: Seq[Path], added: Seq[Path]) {
      def prependPaths(prefix: String): Differences =
         Differences(missing.map(List(prefix) ++ _), added.map(List(prefix) ++ _))
   }
   object Differences {
      def empty: Differences = Differences(Seq.empty, Seq.empty)
      def merge(diffs: Seq[Differences]): Differences =
         Differences(diffs.flatMap(_.missing), diffs.flatMap(_.added))
   }
   def diff(old: Structure, news: Structure): Differences = (old, news) match {
      case (Leaf, _) => Differences.empty
      case (_, Leaf) => Differences.empty
      case (Array(io), Array(in)) => diff(io, in).prependPaths("*")
      case (Struct(fo), Struct(fn)) =>
         val ko = fo.keySet
         val kn = fn.keySet
         val common = ko & kn
         val removed = ko &~ common
         val added = kn &~ common

         val inner = common.map(key => diff(fo(key), fn(key)).prependPaths(key))
         val local = Differences(removed.map(List(_)).toSeq, added.map(List(_)).toSeq)
         Differences.merge(inner.toSeq ++ Seq(local))
      case (_, _) => throw new RuntimeException("Incompatible structure shapes to compare")
   }
}
