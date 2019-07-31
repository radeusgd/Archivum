package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel.DMUtils
import com.radeusgd.archivum.gui.EditableView
import com.radeusgd.archivum.gui.controls.commonproperties.CommonProperties
import com.radeusgd.archivum.gui.controls.dmbridges.StringBridge
import com.radeusgd.archivum.gui.controls.tablecolumns.{Column, ColumnFactory, SimpleColumn}
import com.radeusgd.archivum.gui.layout.{LayoutFactory, LayoutParseError, ParsedLayout}
import com.radeusgd.archivum.gui.utils.XMLUtils
import com.radeusgd.archivum.languages.ViewLanguage
import javafx.util.Callback
import org.controlsfx.control.textfield.{AutoCompletionBinding, TextFields}
import scalafx.scene.control.TextField

import scala.collection.JavaConverters._
import scala.xml.Node

class AutocompleteTextControl(properties: CommonProperties,
                              sources: List[String],
                              path: List[String],
                              editableView: EditableView)
   extends BaseTextControl(StringBridge, properties.copy(rows = Some(1)), path, editableView) {

   private val actualTextField = textField.asInstanceOf[TextField] // we force the parent to create a TextFIeld so this is safe

   private val suggestionProvider: Callback[AutoCompletionBinding.ISuggestionRequest, java.util.Collection[String]] =
      (request: AutoCompletionBinding.ISuggestionRequest) => {
         val repo = editableView.repo
         val hint = request.getUserText

         val suggestions = sources.flatMap(
            source =>
               try {
                  repo.fetchAutocompletions(DMUtils.parsePath(source), hint)
               } catch {
                  case e: Throwable =>
                     println(e)
                     com.radeusgd.archivum.gui.utils.reportException("!!!", e)
                     throw e
               }
         ).toSet

         val withoutCurrent = suggestions - hint

         withoutCurrent.asJavaCollection
      }

   TextFields.bindAutoCompletion(actualTextField, suggestionProvider)
}

object AutocompleteTextControlFactory extends LayoutFactory {
   override def fromXML(xmlnode: xml.Node, ev: EditableView, prefix: List[String]): Either[LayoutParseError, ParsedLayout] = {
      if (xmlnode.child != Nil) Left(LayoutParseError("This node shouldn't have any children"))
      else {
         for {
            pathPrim <- XMLUtils.extractPath(xmlnode)
            path = prefix ++ pathPrim
            sourcesText = xmlnode.attribute(ViewLanguage.AutocompletionSources).map(_.text)
               .getOrElse(path.mkString(".")) // suggest itself by default
            sources = sourcesText.split(';').toList
            properties <- CommonProperties.parseXML(xmlnode)
            node = new AutocompleteTextControl(properties, sources, path, ev)
         } yield ParsedLayout(node, Seq(node))
      }
   }

   override val nodeType: String = ViewLanguage.AutocompleteTextField
}

object AutocompleteTextColumnFactory
   extends ColumnFactory {
   override def fromXML(xmlnode: Node, ev: EditableView): Either[LayoutParseError, Column] =
      if (xmlnode.child != Nil) Left(LayoutParseError(xmlnode, "This node shouldn't have any children"))
      else {
         val path = XMLUtils.extractPath(xmlnode).getOrElse(Nil)
         for {
            sourcesText <- xmlnode.attribute(ViewLanguage.AutocompletionSources).map(_.text)
               .toRight(LayoutParseError("Autocompletion sources missing"))
            sources = sourcesText.split(';').toList
            properties <- CommonProperties.parseXML(xmlnode)
            moddedProps = properties.copy(label = "")
         } yield new SimpleColumn(
            properties.label,
            (basePath: List[String], ev: EditableView) =>
               new AutocompleteTextControl(moddedProps, sources, basePath ++ path, ev)
         )
      }

   override val nodeType: String = ViewLanguage.AutocompleteTextColumn
}
