package com.radeusgd.archivum.gui.controls.tablecolumns

import com.radeusgd.archivum.gui.controls.dmbridges.ClassicDateBridge
import com.radeusgd.archivum.languages.ViewLanguage

object ClassicDateColumnFactory extends StringBasedColumnFactory(ClassicDateBridge) {
   override val nodeType: String = ViewLanguage.ClassicDateColumn
}
