package com.radeusgd.archivum.gui.controls.tablecolumns

import com.radeusgd.archivum.datamodel.{DMUtils, DMValue}
import com.radeusgd.archivum.gui.controls.TableControl
import com.radeusgd.archivum.gui.controls.dmbridges.StringDMBridge
import com.radeusgd.archivum.gui.layout.LayoutParseError
import com.radeusgd.archivum.gui.utils.XMLUtils

import scala.xml.Node
import scalafx.beans.property.ReadOnlyStringWrapper
import scalafx.scene.control.cell.TextFieldTableCell
import scalafx.scene.control.{TableCell, TableColumn}

// later refactor to StringColumn
class StringBasedColumn(path: List[String], stringDMBridge: StringDMBridge, tableControl: TableControl) extends Column[String] {

   private val getter: DMValue => DMValue = DMUtils.makeGetter(path)
   private val setter: (DMValue, DMValue) => DMValue = DMUtils.makeValueSetter(path)
   // TODO
   cellValueFactory = { x => ReadOnlyStringWrapper(stringDMBridge.fromDM(getter(x.value))) }

   onEditCommit = ev => {
      val newValue = stringDMBridge.fromString(ev.getNewValue)
      tableControl.update(ev.getTablePosition.getRow, setter(_, newValue))
   }

   def cellFactoryF(t: TableColumn[DMValue, String]): TableCell[DMValue, String] =
      new TextFieldTableCell[DMValue, String]() {
         // TODO
      }

   cellFactory = cellFactoryF _

   editable = true

   private val label: String = path.lastOption.getOrElse("") // TODO handle label from XML

   text = label
}

abstract class StringBasedColumnFactory(bridge: StringDMBridge) extends ColumnFactory {
   override def fromXML(xmlnode: Node, tableControl: TableControl): Either[LayoutParseError, Column[String]] = {
      val path = XMLUtils.extractPath(xmlnode).fold(_ => Nil, identity) // we extract path and if there's none we assume we will be editing the whole object
      Right(new StringBasedColumn(path, bridge, tableControl))
   }
}