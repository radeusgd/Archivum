package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel.DMValue
import com.radeusgd.archivum.datamodel.LiftDMValue._

object Aggregations {
   def count(rows: Seq[DMValue]): DMValue =
      rows.length
}
