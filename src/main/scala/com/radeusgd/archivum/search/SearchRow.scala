package com.radeusgd.archivum.search

import com.radeusgd.archivum.datamodel.DMValue
import com.radeusgd.archivum.persistence.DBUtils.Rid

class SearchRow(val rid: Rid, humanIdLambda: () => Int, val record: DMValue) {
   lazy val humanId: Int = humanIdLambda()
}
