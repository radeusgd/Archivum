package com.radeusgd.archivum.gui.controls

import java.io.{File, FileInputStream}
import java.nio.file.{Files, Path, Paths, StandardCopyOption}

import com.radeusgd.archivum.datamodel._
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.dmbridges.StringDMBridge
import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.layout.HBox
import com.radeusgd.archivum.gui.controls.dmbridges.StringBridge
import scalafx.scene.image.{Image, ImageView}
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter
import java.security.{DigestInputStream, MessageDigest}

// this is a hacky implementation that stores images in a folder by a hash and stores the name in the DB
// TODO divide storage and presentation
class HackyImageControl(val label: String, path: List[String],
                        protected val editableView: EditableView) extends HBox with BoundControl {

   private val bridge: StringDMBridge = StringBridge


   protected val fieldGetter: DMValue => DMValue = DMUtils.makeGetter(path)
   protected val fieldSetter: (DMValue, DMValue) => DMValue = DMUtils.makeSetter(path)

   // TODO awful hacks
   protected def fromValue(v: DMValue): String = bridge.fromDM(v)
   protected def toValue(s: String): DMValue = bridge.fromString(s)

   private val image: ImageView = new ImageView
   private val changeImageButton: Button = new Button("E")

   children = Seq(image, changeImageButton)

   changeImageButton.onAction = handle {
      val fileChooser = new FileChooser {
         title = "Open image"
         extensionFilters.addAll(
            new ExtensionFilter("Images", Seq("*.jpg", "*.jpeg", "*.png", "*.tiff", "*.gif")),
            new ExtensionFilter("All file types", "*")
         )
      }

      Option(fileChooser.showOpenDialog(delegate.getScene.getWindow)).foreach(file => {
         val fpath = file.getAbsolutePath
         // based on https://stackoverflow.com/a/41643076
         val buffer = new Array[Byte](8192)
         val sha = MessageDigest.getInstance("SHA-1")
         val dis = new DigestInputStream(new FileInputStream(file), sha)
         try {
            //noinspection ScalaStyle
            while (dis.read(buffer) != -1) {}
         } finally { dis.close() }

         val digest = sha.digest.map("%02x".format(_)).mkString
         val ext = fpath.split('.').last

         val newValue = digest + "." + ext
         val newPath = makeUrl(newValue)


         // ensure directory exists (TODO move to separate function)
         val directory = new File("images/")
         if (!directory.exists()) {
            directory.mkdir()
         }

         Files.copy(Paths.get(fpath), Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING)

         setCurrentImage(newValue)
         editableView.update(fieldSetter(_, toValue(newValue)))
      })
   }


   private def makeUrl(imagName: String) = "images/" + imagName

   private def makeImage(name: String): Image = {
      if (name != "")
         new Image(new File(makeUrl(name)).toURI.toString)
      else null
   }

   private def setCurrentImage(name: String): Unit = {
      image.setImage(makeImage(name))
      image.setPreserveRatio(true)
      image.setFitWidth(100)
      // TODO this could probably be done once but I'm in a hurry :(
      image.onMouseClicked = handle {
         val alert = new Alert(Alert.AlertType.Information)
         val bigImgView = new ImageView(makeImage(name))
         val scrollPane = new ScrollPane
         scrollPane.prefViewportHeight = 1000
         scrollPane.prefViewportWidth = 1800
         scrollPane.content = bigImgView
         alert.setHeaderText(null)
         alert.setTitle("Preview")
         alert.getDialogPane.setPrefSize(1600, 800)
         alert.getDialogPane.setContent(scrollPane)
         alert.show()
      }
   }

   override def refreshBinding(newValue: DMStruct): Unit = {
      /*
       TODO warning - this can be destructive
       (if the element had something else than String or Null,
       but typechecking should make sure this won't happen
        */
      val imagename = bridge.fromDM(fieldGetter(newValue))
      setCurrentImage(imagename)
   }

   private val errorTooltip = Tooltip("")

   override def refreshErrors(errors: List[ValidationError]): List[ValidationError] = {
      val (myErrors, otherErrors) = errors.partition(_.getPath == path)
      if (myErrors.isEmpty) {
         Tooltip.uninstall(image, errorTooltip)
         errorTooltip.hide()
      } else {
         Tooltip.install(image, errorTooltip)
         val texts = myErrors.map(_.getMessage)
         errorTooltip.text = texts.mkString("\n")
         val pos = image.localToScreen(0, 0)
         errorTooltip.show(image, pos.getX, pos.getY)
      }

      otherErrors
   }
}