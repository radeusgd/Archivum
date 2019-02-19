package com.radeusgd.archivum.search.commonfeatures

import com.radeusgd.archivum.datamodel.DMValue
import com.radeusgd.archivum.persistence.DBUtils.Rid
import com.radeusgd.archivum.search.SearchRow

case class CommonFeatures(list: List[Int]) {
   def amount: Int = list.sum
}

object CommonFeatures {
   type Extractor = DMValue => Option[String]
   type FeatureGroup = Seq[Extractor]
   type FeatureSet = Seq[FeatureGroup]

   private def extractFeatures(fg: FeatureGroup, r: DMValue): Seq[String] =
      for {
         extractor <- fg
         feature <- extractor(r)
      } yield feature

   private def countCommonFeaturesFromGroup(fg: FeatureGroup, r1: DMValue, r2: DMValue): Int = {
      val features1 = extractFeatures(fg, r1)
      val features2 = extractFeatures(fg, r2)

      features1.count(f => features2.contains(f)) // count how many features from f1 are present in f2
   }

   def countCommonFeatures(fs: FeatureSet, r1: DMValue, r2: DMValue): CommonFeatures = {
      CommonFeatures(fs.map(countCommonFeaturesFromGroup(_, r1, r2)).toList)
   }
}

object FeaturesLexicographicOrder extends Ordering[CommonFeatures] {
   override def compare(x: CommonFeatures, y: CommonFeatures): Int = {
      if (x.list.length != y.list.length) throw new IllegalArgumentException("Common feature sets have to be of equal length")
      compareLex(x.list, y.list)
   }

   private def compareLex(x: List[Int], y: List[Int]): Int = (x, y) match {
      case (hx :: tx, hy :: ty) =>
         if (hx == hy) compareLex(tx, ty)
         else hx.compare(hy)
      case (Nil, Nil) => 0
      //      case (_, Nil) => 1
      //      case (Nil, _) => -1
      case _ => throw new IllegalArgumentException("Both lists should be of same length")
   }
}

object FeaturesEqualImportanceOrder extends Ordering[CommonFeatures] {
   override def compare(x: CommonFeatures, y: CommonFeatures): Int =
      x.amount compare y.amount
}

class CommonFeaturesRow(val commonFeatures: CommonFeatures,
                        override val rid: Rid,
                        humanIdLambda: () => Int,
                        override val record: DMValue)
   extends SearchRow(rid, humanIdLambda, record)
