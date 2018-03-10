package com.radeusgd.archivum.gui.controls.tablecolumns

import com.radeusgd.archivum.gui.controls.dmbridges.DateBridge
import com.radeusgd.archivum.languages.ViewLanguage

object DateColumnFactory extends StringBasedColumnFactory(DateBridge) {
   override val nodeType: String = ViewLanguage.DateColumn
}
