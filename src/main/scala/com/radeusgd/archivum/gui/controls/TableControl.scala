package com.radeusgd.archivum.gui.controls

import cats.implicits._
import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.datamodel.types.{ArrayField, FieldType, StructField}
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.tablecolumns._
import com.radeusgd.archivum.gui.layout.{LayoutFactory, LayoutParseError, ParsedLayout}
import com.radeusgd.archivum.gui.utils.XMLUtils
import com.radeusgd.archivum.languages.ViewLanguage
import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{GridPane, VBox}

import scala.xml.Node


class TableControl(/* TODO some params */
                   childrenxml: Seq[Node],
                   path: List[String],
                   protected val editableView: EditableView)
   extends VBox with BoundControl {

   private def makeMyColumns(): Seq[Column] = {
      type EitherLayout[A] = Either[LayoutParseError, A]
      childrenxml.map(TableControlFactory.makeColumn(editableView)).toList.sequence[EitherLayout, Column]
         .toTry.get // throw on failure (that's the only way to get out of the constructor)
   }

   val myColumns: Seq[Column] = makeMyColumns()
   val headerRow: Seq[Label] = myColumns.map(col => new Label(col.headerName) {
      padding = Insets(0, 5, 0, 0)
   })
   var rows: Vector[Seq[Column.Cell]] = Vector()

   def makeRow(ith: Int): Seq[Column.Cell] =
      myColumns.map(_.createControl(path ++ List(ith.toString), editableView))

   override def refreshBinding(newValue: DMStruct): Unit = {
      val arr = getter(newValue).asInstanceOf[DMArray]

      // update amount of rows to match array
      if (arr.length < rows.length) {
         rows = rows.take(arr.length)
      } else if (arr.length > rows.length) {
         rows = rows ++ (rows.length until arr.length).map(makeRow)
      }

      // update grid (TODO more lazy)
      fieldsContainer.children.clear()
      fieldsContainer.addRow(0, headerRow.map(_.delegate):_*)
      rows.zipWithIndex.foreach({ case (row, idx) =>
         val btn: Button = new Button("x") {
            onAction = handle {
               // this is not exactly the way I'd like to do it
               editableView.update(v => removeRow(idx)(v.asInstanceOf[DMStruct])) // please forgive me
               refreshBinding(editableView.modelInstance)
            }
            focusTraversable = false
         }
         val nodes = row.map(_.delegate) ++ Seq(btn.delegate)
         fieldsContainer.addRow(idx + 1, nodes:_*)
      })

      // update all rows inside
      rows.flatten.foreach(_.refreshBinding(newValue))
   }

   private val getter: DMStruct => DMValue = DMUtils.makeGetter(path)

   private val setter: (DMValue, DMValue) => DMValue = DMUtils.makeSetter(path)

   def update(idx: Int, upd: (DMValue) => DMValue): Unit = {
      def rupd(root: DMValue): DMValue = {
         val oldArray = getter(root.asInstanceOf[DMStruct]).asInstanceOf[DMArray]
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
      /*def helper(acc: DMValue, column: Column): DMValue = {
         // creating cells just for accessing augmentFreshValue is an overkill, but not much can be done about it
         // because I decided to have default / sticky in the view layer instead of model layer (which could be put into FieldType)
         val tmpCell = column.createControl(Nil, editableView)
         tmpCell.augmentFreshValue(acc)
      }
      val newInstance =
         myColumns.foldLeft(getMyFieldType.makeEmpty)(helper)*/
      val newInstance = getMyFieldType.makeEmpty
      val newArray = oldArray.appended(newInstance)
      setter(root, newArray).asInstanceOf[DMStruct] // FIXME
   }

   private def removeRow(idx: Int)(root: DMStruct): DMStruct = {
      val oldArray = getter(root).asInstanceOf[DMArray]
      val newArray = oldArray.without(idx)
      println("Removing " + idx)
      setter(root, newArray).asInstanceOf[DMStruct] // FIXME
   }

   private val addButton: Button = new Button("+") {
      onAction = handle {
         // this is not exactly the way I'd like to do it
         editableView.update(v => insertRow(v.asInstanceOf[DMStruct]))
         refreshBinding(editableView.modelInstance)
      }
      focusTraversable = false
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
      TextColumnFactory,
      DateColumnFactory,
      ClassicDateColumnFactory,
      YearDateColumnFactory,
      ChoiceColumnFactory,
      IntegerColumnFactory,
      HackyImageColumnFactory,
      AutocompleteTextColumnFactory
   )

   private val columnFactories: Map[String, ColumnFactory] =
      (columnFactoriesList map { p => (p.nodeType, p) }).toMap

   def makeColumn(ev: EditableView)(xmlnode: Node): Either[LayoutParseError, Column] = {
      columnFactories.get(xmlnode.label.toLowerCase)
         .toRight(LayoutParseError("Unknown column type " + xmlnode.label))
         .flatMap(f => f.fromXML(xmlnode, ev))
   }

   override val nodeType: String = ViewLanguage.TableRoot
}