package com.radeusgd.archivum.search

import com.radeusgd.archivum.datamodel.{DMString, DMUtils}
import com.radeusgd.archivum.utils.AsInt
import scalafx.scene.control.{Label, TextField}
import scalafx.scene.layout.{HBox, VBox}

sealed trait SearchCondition {
   // TODO
}

case class ExactMatch(path: String, value: String) extends SearchCondition
case class YearDateMatch(path: String, year: Int) extends SearchCondition
case class FulltextMatch(value: String) extends SearchCondition
// TODO possibly add prefix match ?

object SearchCondition {
   def toHumanText(sc: SearchCondition): String = {
      // TODO
      "TODO"
   }
}

trait ConditionGiver {
   def getCurrentCondition(): Option[SearchCondition]
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

// TODO add support for types other than string!
class EqualConditionField(name: String, width: Int, path: String)
extends TextFieldCondition(name, width, TextFieldCondition.wrapNonEmptyMake(ExactMatch(path, _)))

class FulltextConditionField(name: String, width: Int)
extends TextFieldCondition(name, width, TextFieldCondition.wrapNonEmptyMake(FulltextMatch))

class YearConditionField(name: String, width: Int, path: String)
extends TextFieldCondition(name, width, (s: String) => {
   s match {
      case AsInt(year) => Some(YearDateMatch(path, year))
      case _ => None
   }
})