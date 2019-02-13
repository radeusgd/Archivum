package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel.{DMUtils, DMValue}
import com.radeusgd.archivum.datamodel.LiftDMValue._

object ClassicAggregations {
   type Aggregation = Seq[DMValue] => DMValue
   def count: Aggregation =
      _.length

   def countPredicate(pred: DMValue => Boolean): Aggregation =
      _.count(pred)

   def countEqual(path: String, value: DMValue): Aggregation = {
      val getter = DMUtils.makeGetter(path)
      countPredicate(getter(_) == value)
   }

   def countTransposed(path: String, traits: Seq[(String, DMValue)], default: Option[String]): Seq[(String, Aggregation)] = {
      val traitsAggregations =
         traits.map { case (name, value) => name -> ClassicAggregations.countEqual(path, value) }
      default match {
         case Some(defaultName) =>
            val countedVals = Set(traits.map(_._2):_*)
            val getter = DMUtils.makeGetter(path)
            val pred: DMValue => Boolean = v =>
               !countedVals.contains(getter(v))
            traitsAggregations ++ Seq(defaultName -> ClassicAggregations.countPredicate(pred))
         case None => traitsAggregations
      }
   }
}
