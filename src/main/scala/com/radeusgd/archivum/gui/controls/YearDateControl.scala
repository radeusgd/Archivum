package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.languages.ViewLanguage
import com.radeusgd.archivum.gui.controls.dmbridges.ClassicYearDateBridge

class YearDateControl(label: String, path: List[String], editableView: EditableView)
   extends BaseTextControl(ClassicYearDateBridge, label, path, editableView)

object YearDateControlFactory extends SimpleControlFactory(new YearDateControl(_, _, _)) {
   override val nodeType: String = ViewLanguage.YearDateField
}
