package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel.{DMUtils, DMValue}
import com.radeusgd.archivum.datamodel.LiftDMValue._

object Aggregations {
   type Aggregation = Seq[DMValue] => DMValue
   def count: Aggregation =
      _.length

   def countPredicate(pred: DMValue => Boolean): Aggregation =
      _.count(pred)

   def countEqual(path: String, value: DMValue): Aggregation = {
      val getter = DMUtils.makeGetter(path)
      countPredicate(getter(_) == value)
   }
}
