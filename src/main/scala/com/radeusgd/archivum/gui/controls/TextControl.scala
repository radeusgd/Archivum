package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.dmbridges.StringBridge
import com.radeusgd.archivum.languages.ViewLanguage

class TextControl(label: String, path: List[String], editableView: EditableView)
   extends BaseTextControl(StringBridge, label, path, editableView)

object TextControlFactory extends SimpleControlFactory(new TextControl(_, _, _)) {
   override val nodeType: String = ViewLanguage.TextField
}
