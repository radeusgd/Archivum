package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.commonproperties.{CommonControlFactory, CommonProperties}
import com.radeusgd.archivum.datamodel.dmbridges.ClassicDateBridge
import com.radeusgd.archivum.languages.ViewLanguage

class ClassicDateControl(properties: CommonProperties, path: List[String], editableView: EditableView)
   extends BaseTextControl(ClassicDateBridge, properties, path, editableView)

object ClassicDateControlFactory extends CommonControlFactory(new ClassicDateControl(_, _, _)) {
   override val nodeType: String = ViewLanguage.ClassicDateField
}
