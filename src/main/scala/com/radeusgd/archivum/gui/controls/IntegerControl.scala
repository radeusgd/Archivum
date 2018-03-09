package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.dmbridges.IntegerBridge
import com.radeusgd.archivum.languages.ViewLanguage

class IntegerControl(label: String, path: List[String], editableView: EditableView)
   extends BaseTextControl(IntegerBridge, label, path, editableView)

object IntegerControlFactory extends SimpleControlFactory(new IntegerControl(_, _, _)) {
   override val nodeType: String = ViewLanguage.IntegerField
}
