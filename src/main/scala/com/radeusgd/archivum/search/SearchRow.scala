package com.radeusgd.archivum.search

import com.radeusgd.archivum.datamodel.DMValue
import com.radeusgd.archivum.persistence.DBUtils.Rid

case class SearchRow(rid: Rid, humanId: Int, record: DMValue)
