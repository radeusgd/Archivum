package com.radeusgd.archivum.gui.controls.dmview

import com.radeusgd.archivum.datamodel.DMValue

trait StringDMBridge {
   def fromString(s: String): DMValue
   def fromDM(v: DMValue): String
}
