package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.dmbridges.StringBridge
import com.radeusgd.archivum.languages.ViewLanguage

class TextControl(properties: CommonProperties, path: List[String], editableView: EditableView)
   extends BaseTextControl(StringBridge, properties, path, editableView)

object TextControlFactory extends CommonControlFactory(new TextControl(_, _, _)) {
   override val nodeType: String = ViewLanguage.TextField
}
