package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel.DMStruct

trait BoundControl {
   def refreshBinding(newValue: DMStruct): Unit
}