package com.radeusgd.archivum.gui.layout

import com.radeusgd.archivum.gui.controls.LayoutDefaults
import com.radeusgd.archivum.languages.ViewLanguage

import scalafx.scene.layout.HBox

object HBoxFactory extends AggregateFactory(new HBox(_: _*) {
   spacing = LayoutDefaults.defaultSpacing
}) {
   override val nodeType: String = ViewLanguage.Hbox
}
