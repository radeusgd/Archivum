package com.radeusgd.archivum.gui.scenes

import java.io.File

import com.radeusgd.archivum.conversion.{JsonStructure, ModelStructure, Structure}
import com.radeusgd.archivum.datamodel.{DMStruct, DMUtils}
import com.radeusgd.archivum.gui.utils
import com.radeusgd.archivum.persistence.Repository
import com.radeusgd.archivum.utils.IO
import javafx.concurrent.Task

import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.Settings
import spray.json._

import scala.util.control.NonFatal
import scalafx.Includes.handle
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.layout.{HBox, VBox}
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

class ImportRepository(val repository: Repository, parentScene: Scene) extends Scene {

   private def chooseOpenFile(): Option[File] = {
      val fileChooser = new FileChooser {
         title = "Import from JSON"
         extensionFilters.addAll(
            new ExtensionFilter("JSON list", "*.dbjson"),
            new ExtensionFilter("All Files", "*.*")
         )
      }

      Option(fileChooser.showOpenDialog(delegate.getWindow))
   }

   //noinspection ScalaStyle
   private def importJson(file: File): Unit = {
      val task = new Task[Unit] {
         override def call(): Unit = {
            val source = io.Source.fromFile(file)
            try {
               updateMessage("Wczytywanie pliku")
               val lines = source.getLines().toSeq

               var progress = 0
               val todo = 3 * lines.length
               def bumpProgress(): Unit = {
                  progress += 1
                  updateProgress(progress, todo)
               }
               updateMessage("Wstępne przetwarzanie")
               val preprocessor = preparePreprocessingFunction()
               val jsons = lines.map(l => {
                  bumpProgress()
                  preprocessor(l.parseJson)
               })

               updateMessage("Analiza struktury danych")
               val newStructure = ModelStructure.extract(repository.model)
               val oldStructure = JsonStructure.unify(jsons)

               val diff = Structure.diff(oldStructure, newStructure)

               def pathToStr(p: List[String]): String = p.mkString(".")

               val removed =
                  if (diff.missing.isEmpty) ""
                  else "Pola, które zostaną usunięte:\n" ++ diff.missing.map(pathToStr).mkString("\n")

               val added =
                  if (diff.added.isEmpty) ""
                  else "Nowe pola, zostaną ustawione na puste wartości:\n" ++ diff.added.map(pathToStr).mkString("\n")
               val comment =
                  if (removed.isEmpty && added.isEmpty) "Brak potrzeby konwersji bazy"
                  else removed + "\n\n" + added + "\n\nJeśli mimo to chcesz kontynuować naciśnij OK."

               val question = "Czy napewno chcesz zaimportować " + jsons.length + " rekordów?"

               var alert: Alert = null
               Platform.runLater({
                  val a = new Alert(AlertType.Confirmation) {
                     headerText = question
                     contentText = comment
                  }
                  a.showAndWait()
                  alert = a
               })

               //noinspection LoopVariableNotUpdated
               while (alert == null) Thread.`yield`()

               val continue = alert.result.get() == ButtonType.OK.delegate

               if (continue) {
                  updateMessage("Deserializacja danych")
                  val fixer =
                     (js: JsValue) => JsonStructure.makeEmptyFields(repository.model, diff.added)(JsonStructure.dropFields(diff.missing)(js))

                  val records = jsons.map(json => {
                     bumpProgress()
                     val fixed = fixer(json)
                     repository.model.roottype.fromHumanJson(fixed)
                  })
                  val errors = records.count(_.isLeft)
                  if (errors > 0) {
                     utils.showError("Import nie powiódł się", "W " + errors + " rekordach wystąpił błąd importu, pierwszy z nich zostanie zaraz wyświetlony")
                     records.foreach(er => er.fold(throw _, identity))
                  } else {
                     updateMessage("Zapis danych do bazy")
                     val valid: Seq[DMStruct] = records.map(er => er.fold(throw _, identity))
                     repository.createRecords(valid)
                     progress += lines.length
                     updateProgress(progress, todo)
                     updateMessage("" + valid.length + " rekordów zostało pomyślnie dodanych do bazy")
                  }
               } else {
                  throw new Exception {
                     override def toString: String = "Import anulowany"
                     override def getMessage: String = "Import anulowany"
                  }
               }
            } finally {
               source.close()
            }
         }
      }
      utils.runTaskWithProgress("Import", task)
   }

   val preprocessCode = new TextArea()
   val defaultCode: String =
      """
        |// JsString, JsNull, JsObject, JsArray, JsNumber
        |// Js.Type == JsValue.type
        |// object u {
        |// def getNested(path: String, object: JsValue): JsValue
        |// def setNested(path: String, object: JsValue, value: JsValue): JsValue
        |// def mapNested(path: String, object: JsValue, mapper: JsValue => JsValue): JsValue
        |// def mapArray(array: JsValue, mapper: JsValue => JsValue): JsValue
        |// }
        |(js: Js.Type) => {
        |   js
        |}
      """.stripMargin
   preprocessCode.text = defaultCode
   preprocessCode.editable = true

   def preparePreprocessingFunction(): JsValue => JsValue = {
      val code: String = preprocessCode.text.value
      if (code == defaultCode) identity
      else {
         val settings = new Settings
         settings.usejavacp.value = true
         settings.deprecation.value = true

         val eval = new IMain(settings)
         //val evaluated = eval.beSilentDuring(eval.interpret(clazz))
         //eval.directBind("Helper", Helper) // TODO bind
         eval.directBind("JsObject", JsObject)
         eval.directBind("JsString", JsString)
         eval.directBind("JsNumber", JsNumber)
         eval.directBind("JsNull", JsNull)
         eval.directBind("JsArray", JsArray)
         eval.bind("Js", JsUtils.Js)
         eval.bind("u", JsUtils)

         val evaluated = eval.interpret(code)
         val res = eval.valueOfTerm("res0")
            .getOrElse(throw new RuntimeException("Błąd kompilacji kodu preprocesora, więcej informacji w konsoli"))
            .asInstanceOf[JsValue => JsValue]
         res
      }
   }

   content = new VBox(
      utils.makeGoToButton("< Powrót", parentScene),
      new Label("Zaimporotwane rekordy zostaną dopisane do bazy (rekordy już znajdujące się w bazie pozostaną niezmienione)"),
      new Label("Wspierane jest tylko importowanie z formatu JSON"),
      new HBox(new Label("Preprocess (zaawansowane):"), preprocessCode),
      new Button("Import") {
         onAction = handle {
            chooseOpenFile() match {
               case None => utils.showError("No file selected")
               case Some(file) => importJson(file)
            }
         }
      }
   ) {
      spacing = 5
      padding = Insets(15.0)
   }
}

object JsUtils {
   def getNested(path: String, obj: JsValue): JsValue = {
      val p = DMUtils.parsePath(path)
      def getn(obj: JsValue, p: List[String]): JsValue = p match {
         case Nil => obj
         case f :: rest => getn(obj.asJsObject.fields(f), rest)
      }
      getn(obj, p)
   }
   def setNested(path: String, obj: JsValue, value: JsValue): JsValue =
      mapNested(path, obj, _ => value)
   def mapNested(path: String, obj: JsValue, mapper: JsValue => JsValue): JsValue = {
      val p = DMUtils.parsePath(path)
      def setn(obj: JsValue, p: List[String]): JsValue = p match {
         case Nil => mapper(obj)
         case f :: rest =>
            val fields = obj.asJsObject.fields
            val inner = fields.getOrElse(f, JsObject.empty)
            JsObject(fields.updated(f, setn(inner, rest)))
      }
      setn(obj, p)
   }
   def mapArray(array: JsValue, mapper: JsValue => JsValue): JsValue = array match {
      case JsArray(elems) => JsArray(elems.map(mapper))
      case _ => throw new RuntimeException("Expected an array")
   }
   object Js {
      type Type = JsValue
   }
}