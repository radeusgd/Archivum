package com.radeusgd.archivum.gui.controls

import cats.implicits._
import com.radeusgd.archivum.datamodel.types.{ArrayField, FieldType, StructField}
import com.radeusgd.archivum.datamodel.{DMArray, DMStruct, DMUtils, DMValue}
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.tablecolumns._
import com.radeusgd.archivum.gui.layout.{LayoutFactory, LayoutParseError, ParsedLayout}
import com.radeusgd.archivum.gui.utils.XMLUtils
import com.radeusgd.archivum.languages.ViewLanguage

import scala.xml.Node
import scalafx.Includes._
import scalafx.scene.control.{Button, TableView}
import scalafx.scene.layout.{GridPane, HBox, VBox}

class TableControl(/* TODO some params */
                   childrenxml: Seq[Node],
                   path: List[String],
                   protected val editableView: EditableView)
   extends VBox with BoundControl {

   private def makeMyColumns(): Seq[Column] = {
      type EitherLayout[A] = Either[LayoutParseError, A]
      childrenxml.map(TableControlFactory.makeColumn(this)).toList.sequence[EitherLayout, Column]
         .toTry.get // throw on failure (that's the only way to get out of the constructor)
   }

   val myColumns: Seq[Column] = makeMyColumns()
   val rows: Seq[Seq[Column.Cell]] = Seq()

   override def refreshBinding(newValue: DMStruct): Unit = {
      val arr = getter(newValue).asInstanceOf[DMArray]

      if (arr.length < rows.length) {
         // TODO remove unneeded rows
      } else if (arr.length > rows.length) {
         // TODO create more rows
      }
      //table.items.getValue.setAll(arr.values: _*)
      // TODO update all rows inside
   }

   private val getter: DMStruct => DMValue = DMUtils.makeGetter(path)

   private val setter: (DMStruct, DMValue) => DMStruct = DMUtils.makeSetter(path)

   def update(idx: Int, upd: (DMValue) => DMValue): Unit = {
      def rupd(root: DMStruct): DMStruct = {
         val oldArray = getter(root).asInstanceOf[DMArray]
         val oldElem = oldArray(idx)
         val newElem = upd(oldElem)
         val updatedArray = oldArray.updated(idx, newElem)
         setter(root, updatedArray)
      }

      editableView.update(rupd)
   }

   /*
   private val table: TableView[DMValue] = new TableView[DMValue]() {
      columns.setAll(myColumns.map(_.delegate): _*)
      prefWidth = myColumns.length * 150 // TODO configurable width
   }*/

   // TODO this is quite hacky :(
   @scala.annotation.tailrec
   private def getFieldType(f: FieldType, path: List[String]): FieldType = path match {
      case Nil => f
      case h :: t => getFieldType(f.asInstanceOf[StructField].fieldTypes(h), t) // assume we actually have Structs along the way
   }

   private def getMyFieldType: FieldType =
      getFieldType(editableView.model.roottype, path)
         .asInstanceOf[ArrayField].elementsType

   private def insertRow(root: DMStruct): DMStruct = {
      val oldArray = getter(root).asInstanceOf[DMArray]
      val newArray = oldArray.appended(getMyFieldType.makeEmpty)
      setter(root, newArray)
   }

   private def removeRow(idx: Int)(root: DMStruct): DMStruct = {
      val oldArray = getter(root).asInstanceOf[DMArray]
      val newArray = oldArray.without(idx)
      setter(root, newArray)
   }

   private val addButton: Button = new Button("+") {
      onAction = handle {
         // this is not exactly the way I'd like to do it
         editableView.update(insertRow)
         refreshBinding(editableView.modelInstance)
      }
   }

   private val fieldsContainer: GridPane = new GridPane()
   children = List(fieldsContainer, addButton)
}

object TableControlFactory extends LayoutFactory {
   override def fromXML(xmlnode: Node, ev: EditableView): Either[LayoutParseError, ParsedLayout] = {
      for {
         path <- XMLUtils.extractPath(xmlnode)
         table <- try {
            Right(new TableControl(XMLUtils.properChildren(xmlnode), path, ev))
         } catch {
            case e: LayoutParseError => Left(e)
         }
      } yield ParsedLayout(table, Seq(table))
   }

   private val columnFactoriesList: Seq[ColumnFactory] = Seq(
      TextColumnFactory/*,
      DateColumnFactory,
      ClassicDateColumnFactory*/
   )

   private val columnFactories: Map[String, ColumnFactory] =
      (columnFactoriesList map { p => (p.nodeType, p) }).toMap

   def makeColumn(tc: TableControl)(xmlnode: Node): Either[LayoutParseError, Column] = {
      columnFactories.get(xmlnode.label.toLowerCase)
         .toRight(LayoutParseError("Unknown column type " + xmlnode.label))
         .flatMap(f => f.fromXML(xmlnode, tc))
   }

   override val nodeType: String = ViewLanguage.TableRoot
}