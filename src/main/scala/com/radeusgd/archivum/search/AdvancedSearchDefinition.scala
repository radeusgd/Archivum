package com.radeusgd.archivum.search

import scalafx.scene.Node
import scalafx.scene.control.Button
import scalafx.scene.layout.HBox
import scalafx.Includes.handle

class AdvancedSearchDefinition {
   def getConditions: List[SearchCondition] = Nil // TODO

   private val conditionsBox = new HBox()
   private var conditions = Nil

   val displayNode: Node = new HBox(
      conditionsBox,
      new Button("Dodaj zaawansowane kryteria") {
         onAction = handle {
            // TODO
         }
      }
   )

   def addCondition(sc: SearchCondition) {
      // TODO
   }

}
