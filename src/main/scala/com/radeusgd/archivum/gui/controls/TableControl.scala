package com.radeusgd.archivum.gui.controls

import cats.implicits._
import com.radeusgd.archivum.datamodel.{DMStruct, DMUtils, DMValue}
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.tablecolumns._
import com.radeusgd.archivum.gui.layout.{LayoutFactory, LayoutParseError, ParsedLayout}
import com.radeusgd.archivum.languages.ViewLanguage
import com.radeusgd.archivum.gui.utils.XMLUtils

import scala.xml.Node
import scalafx.scene.control.{Button, TableView}
import scalafx.scene.layout.{HBox, VBox}

class TableControl(/* TODO some params */
                   myColumns: Seq[Column[_]],
                   path: List[String],
                   protected val editableView: EditableView)
   extends VBox with BoundControl {

   override def refreshBinding(newValue: DMStruct): Unit = {
      //myColumns.foreach(_.refreshBinding())
   }

   private val getter: DMStruct => DMValue = DMUtils.makeGetter(path)

   private val setter: (DMStruct, DMValue) => DMStruct = DMUtils.makeSetter(path)

   def update(upd: (DMValue) => DMValue): Unit = editableView.update(s => setter(s, upd(getter(s))))

   private val table: TableView[DMValue] = new TableView[DMValue]() {
      columns.setAll(myColumns.map(_.delegate): _*)
   }

   private val addButton = new Button("+")

   private val removeButton = new Button("x")

   children = List(table, new HBox(removeButton, addButton))
}

object TableControlFactory extends LayoutFactory {
   override def fromXML(xmlnode: Node, ev: EditableView): Either[LayoutParseError, ParsedLayout] = {
      type EitherLayout[A] = Either[LayoutParseError, A]
      lazy val children = XMLUtils.properChildren(xmlnode).map(makeColumn(ev)).toList.sequence[EitherLayout, Column[_]]

      for {
         path <- XMLUtils.extractPath(xmlnode)
         children <- children
         table = new TableControl(children, path, ev)
      } yield ParsedLayout(table, Seq(table))
   }

   // TODO may actually need CellFactories instead...
   private val columnFactoriesList: Seq[ColumnFactory] = Seq(
      TextColumnFactory,
      DateColumnFactory,
      ClassicDateColumnFactory
   )

   private val columnFactories: Map[String, ColumnFactory] =
      (columnFactoriesList map { p => (p.nodeType, p) }).toMap

   private def makeColumn(ev: EditableView)(xmlnode: Node): Either[LayoutParseError, Column[_]] = {
      columnFactories.get(xmlnode.label.toLowerCase)
         .toRight(LayoutParseError("Unknown column type " + xmlnode.label))
         .flatMap(f => f.fromXML(xmlnode, ???)) // TODO
   }

   override val nodeType: String = ViewLanguage.TableRoot
}