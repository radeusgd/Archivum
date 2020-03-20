package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.commonproperties.{CommonControlFactory, CommonProperties}
import com.radeusgd.archivum.datamodel.dmbridges.DateBridge
import com.radeusgd.archivum.languages.ViewLanguage

class DateControl(properties: CommonProperties, path: List[String], editableView: EditableView)
   extends BaseTextControl(DateBridge, properties, path, editableView)

object DateControlFactory extends CommonControlFactory(new DateControl(_, _, _)) {
   override val nodeType: String = ViewLanguage.DateField
}
