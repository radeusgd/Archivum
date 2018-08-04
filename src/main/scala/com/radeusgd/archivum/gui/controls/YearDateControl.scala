package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.languages.ViewLanguage
import com.radeusgd.archivum.gui.controls.dmbridges.ClassicYearDateBridge

class YearDateControl(properties: CommonProperties, path: List[String], editableView: EditableView)
   extends BaseTextControl(ClassicYearDateBridge, properties, path, editableView)

object YearDateControlFactory extends CommonControlFactory(new YearDateControl(_, _, _)) {
   override val nodeType: String = ViewLanguage.YearDateField
}
