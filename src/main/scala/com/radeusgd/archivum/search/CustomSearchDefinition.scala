package com.radeusgd.archivum.search

import com.radeusgd.archivum.datamodel.dmbridges.StringDMBridge
import com.radeusgd.archivum.datamodel.types.StructField
import com.radeusgd.archivum.datamodel.{DMError, DMUtils, DMValue, Model}
import javafx.scene.control.{ButtonType, Dialog}
import scalafx.scene.Node
import scalafx.scene.control.{Button, DialogPane, Label, ProgressBar, SelectionMode, TextField, TreeItem, TreeView}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.Includes.handle
import scalafx.event.subscriptions.Subscription
import scalafx.scene.paint.Color
import scalafx.stage.Modality

import scala.collection.mutable.ListBuffer

class CustomSearchDefinition(model: Model) {
   def getConditions: List[SearchCondition] = conditions.toList

   private val conditionsBox = new VBox()
   private val conditions = ListBuffer.empty[SearchCondition]

   case class TreeElem(name: String, path: List[String]) {
      override def toString: String = name
   }
   private def makeTreeViewForModel(): TreeView[TreeElem] = {
      type TI = TreeItem[TreeElem]
      val tv = new TreeView[TreeElem]()
      val root = new TI(TreeElem(".", Nil))
      // TODO this function doesn't handle Arrays because they are not supported for searching in the backend
      def processSubTree(rootType: StructField, rootItem: TI, path: List[String]): Unit = {
         for ((name, typ) <- rootType.fieldTypes) {
            val item = new TI(TreeElem(name, (name :: path).reverse))
            rootItem.getChildren.add(item)

            typ match {
               case sf: StructField => processSubTree(sf, item, name :: path)
               // case ar: ArrayField => ???
               case _ =>
            }
         }
      }
      processSubTree(model.roottype, root, Nil)
      root.expanded = true
      tv.setRoot(root)
      tv
   }

   //noinspection ScalaStyle
   private def openCustomConditionDialog(): Unit = {
      val dialog = new Dialog[Void]
      dialog.initModality(Modality.None)
      dialog.setWidth(500)
      dialog.setHeight(80)

      val pane = new DialogPane()
      dialog.setDialogPane(pane)
      pane.getButtonTypes.add(ButtonType.CLOSE)

      var selectedPath: Option[List[String]] = None
      var dmbridge: Option[StringDMBridge] = None
      var dmvalue: Option[DMValue] = None

      val valueTextField = new TextField()
      valueTextField.disable = true
      var currentSub: Option[Subscription] = None
      val confirmBtn = new Button("Dodaj warunek") {
         onAction = handle {
            (selectedPath, dmvalue) match {
               case (Some(path), Some(v)) =>
                  addCondition(ExactMatch(path.mkString("."), v))
                  dialog.hide()
               case _ =>
            }
         }
      }
      confirmBtn.disable = true
      val cancelBtn = new Button("Anuluj") {
         onAction = handle {
            dialog.hide()
         }
      }
      val choiceLabel = new Label("Wybierz pole powyżej")
      val errorLabel = new Label()
      errorLabel.setTextFill(Color.Red)

      val treeView = makeTreeViewForModel()
      def handlePathSelected(path: List[String]): Unit = {
         errorLabel.text = ""
         selectedPath = Some(path)
         currentSub.foreach(_.cancel())
         valueTextField.disable = false
         confirmBtn.disable = false
         choiceLabel.text = "Wybrane pole: " + path.mkString(".")
         dmvalue = None
         dmbridge = model.defaultBridgeForField(path)
         dmbridge match {
            case None =>
               errorLabel.text = "Wybrane pole nie jest przeznaczone do wyszukiwania"
               valueTextField.disable = true
               confirmBtn.disable = true
            case Some(bridge) =>
               val sub = valueTextField.text.onChange((_, _, _) => {
                  val v = bridge.fromString(valueTextField.text.value)
                  v match {
                     case DMError(message) =>
                        dmvalue = None
                        errorLabel.text = message
                        confirmBtn.disable = true
                     case valid =>
                        dmvalue = Some(valid)
                        errorLabel.text = ""
                        confirmBtn.disable = false
                  }
               })
               currentSub = Some(sub)
         }
         valueTextField.text = ""
      }
      treeView.getSelectionModel.selectionModeProperty().setValue(SelectionMode.Single)
      treeView.getSelectionModel.selectedItemProperty().addListener((_, _, _) => {
         val item = treeView.getSelectionModel.getSelectedItem
         if (item != null) {
            handlePathSelected(item.getValue.path)
         }
      })

      pane.content = new VBox(7,
         treeView,
         choiceLabel,
         new HBox(5, new Label("Szukana wartość"), valueTextField),
         errorLabel,
         new HBox(5, confirmBtn, cancelBtn)
      )
      dialog.setTitle("Dodawanie warunku wyszukiwania")

      dialog.show()
   }

   val displayNode: Node = new VBox(
      conditionsBox,
      new Button("Dodaj zaawansowane kryteria") {
         onAction = handle {
            openCustomConditionDialog()
         }
      }
   )

   private def rerenderConditionBox(): Unit = {
      def makeConditionControl(sc: SearchCondition, index: Int): Node = {
         val label = new Label(sc.toHumanText)
         val btn = new Button("X") {
            onAction = handle {
               conditions.remove(index)
               rerenderConditionBox()
            }
         }
         new HBox(5, label, btn)
      }

      conditionsBox.children = {
         for ((c, i) <- conditions.zipWithIndex) yield makeConditionControl(c, i)
      }
   }

   def addCondition(sc: SearchCondition) {
      conditions.append(sc)
      rerenderConditionBox()
   }
}
