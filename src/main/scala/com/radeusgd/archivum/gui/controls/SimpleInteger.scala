package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.dmview.IntegerBridge
import com.radeusgd.archivum.languages.ViewLanguage

class SimpleInteger(label: String, path: List[String], editableView: EditableView)
   extends BaseTextControl(IntegerBridge, label, path, editableView)

object SimpleIntegerFactory extends SimpleControlFactory(new SimpleInteger(_, _, _)) {
   override val nodeType: String = ViewLanguage.IntegerField
}
