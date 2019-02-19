package com.radeusgd.archivum.search.commonfeatures

import com.radeusgd.archivum.search.{ResultColumn, ResultsDisplayColumnFactory}
import scalafx.beans.property.ReadOnlyStringWrapper
import scalafx.scene.control.TableColumn

class CommonFeaturesColumnFactory(columns: Seq[ResultColumn])
extends ResultsDisplayColumnFactory[CommonFeaturesRow](columns) {

   private def makeCommonFeaturesCountColumn(): TableColumn[CommonFeaturesRow, String] =
      new TableColumn[CommonFeaturesRow, String]() {
         prefWidth = 50
         text = "Punkty wspólne"
         cellValueFactory =
            row => ReadOnlyStringWrapper(row.value.commonFeatures.amount.toString)
      }

   override def makeColumns: List[TableColumn[CommonFeaturesRow, String]] =
      makeCommonFeaturesCountColumn() :: super.makeColumns

}
