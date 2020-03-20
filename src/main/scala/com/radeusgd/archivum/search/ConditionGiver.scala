package com.radeusgd.archivum.search

import com.radeusgd.archivum.datamodel.{DMString, DMUtils, DMValue}
import com.radeusgd.archivum.utils.AsInt
import scalafx.scene.control.{Label, TextField}
import scalafx.scene.layout.{HBox, VBox}

sealed trait SearchCondition {
   def toHumanText: String
}

case class ExactMatch(path: String, value: DMValue) extends SearchCondition {
   override def toHumanText: String = path + " = " + value.toString
}
case class YearDateMatch(path: String, year: Int) extends SearchCondition {
   override def toHumanText: String = path + ".rok = " + year
}
case class FulltextMatch(value: String) extends SearchCondition {
   override def toHumanText: String = "'" + value + "' występuje gdziekolwiek w rekordzie"
}
// TODO possibly add prefix match ?


trait ConditionGiver {
   def getCurrentCondition: Option[SearchCondition]
}

class TextFieldCondition(name: String, textFieldWidth: Int, makeCondition: String => Option[SearchCondition]) extends VBox with ConditionGiver {

   private val textField = new TextField() {
      prefWidth = textFieldWidth
   }

   fillWidth = false
   children = Seq(
      new Label(name),
      textField,
   )

   override def getCurrentCondition(): Option[SearchCondition] = makeCondition(textField.text.value)
}

object TextFieldCondition {
   def wrapNonEmptyMake(makeConditionNonEmpty: String => SearchCondition): String => Option[SearchCondition] =
      s => {
         if (s.isEmpty) None
         else Some(makeConditionNonEmpty(s))
      }
}

// TODO this could use Model.defaultBridgeForField to support all types
class EqualConditionField(name: String, width: Int, path: String)
extends TextFieldCondition(name, width, TextFieldCondition.wrapNonEmptyMake(str => ExactMatch(path, DMString(str))))

class FulltextConditionField(name: String, width: Int)
extends TextFieldCondition(name, width, TextFieldCondition.wrapNonEmptyMake(FulltextMatch))

class YearConditionField(name: String, width: Int, path: String)
extends TextFieldCondition(name, width, (s: String) => {
   s match {
      case AsInt(year) => Some(YearDateMatch(path, year))
      case _ => None
   }
})