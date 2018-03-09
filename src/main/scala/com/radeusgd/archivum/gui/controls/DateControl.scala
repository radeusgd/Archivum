package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.dmbridges.DateBridge
import com.radeusgd.archivum.languages.ViewLanguage

class DateControl(label: String, path: List[String], editableView: EditableView)
   extends BaseTextControl(DateBridge, label, path, editableView)

object DateControlFactory extends SimpleControlFactory(new DateControl(_, _, _)) {
   override val nodeType: String = ViewLanguage.DateField
}
