package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel.{DMInteger, DMNull, DMString, DMValue}
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.languages.ViewLanguage

class SimpleInteger(label: String, path: List[String], editableView: EditableView)
   extends SimpleText(label, path, editableView) {
   override def fromValue(v: DMValue): String = v match {
      case DMInteger(i) => i.toString
      case DMNull => ""
      case _ => throw new RuntimeException("Incompatible type")
   }
   override def toValue(v: String): DMValue =
      if (v == "") DMNull
      else util.Try(v.toInt).fold(_ => DMString("Wrong number format"), DMInteger)
}

object SimpleIntegerFactory extends SimpleControlFactory(new SimpleInteger(_, _, _)) {
   override val nodeType: String = ViewLanguage.IntegerField
}
