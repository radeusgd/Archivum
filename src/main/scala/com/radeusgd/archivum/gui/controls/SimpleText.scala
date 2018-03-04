package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.languages.ViewLanguage
import com.radeusgd.archivum.gui.controls.dmview.StringBridge

class SimpleText(label: String, path: List[String], editableView: EditableView)
   extends BaseTextControl(StringBridge, label, path, editableView)

object SimpleTextFactory extends SimpleControlFactory(new SimpleText(_, _, _)) {
   override val nodeType: String = ViewLanguage.TextField
}
