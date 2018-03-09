package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.dmbridges.ClassicDateBridge
import com.radeusgd.archivum.languages.ViewLanguage

class ClassicDateControl(label: String, path: List[String], editableView: EditableView)
   extends BaseTextControl(ClassicDateBridge, label, path, editableView)

object ClassicDateControlFactory extends SimpleControlFactory(new ClassicDateControl(_, _, _)) {
   override val nodeType: String = ViewLanguage.ClassicDateField
}
