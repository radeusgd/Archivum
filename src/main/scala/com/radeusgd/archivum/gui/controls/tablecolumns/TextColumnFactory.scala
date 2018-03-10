package com.radeusgd.archivum.gui.controls.tablecolumns

import com.radeusgd.archivum.gui.controls.dmbridges.StringBridge
import com.radeusgd.archivum.languages.ViewLanguage

object TextColumnFactory extends StringBasedColumnFactory(StringBridge) {
   override val nodeType: String = ViewLanguage.TextColumn
}
