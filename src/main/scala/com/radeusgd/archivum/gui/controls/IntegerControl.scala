package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.dmbridges.IntegerBridge
import com.radeusgd.archivum.languages.ViewLanguage

class IntegerControl(properties: CommonProperties, path: List[String], editableView: EditableView)
   extends BaseTextControl(IntegerBridge, properties, path, editableView)

object IntegerControlFactory extends CommonControlFactory(new IntegerControl(_, _, _)) {
   override val nodeType: String = ViewLanguage.IntegerField
}
