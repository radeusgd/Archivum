package com.radeusgd.archivum.gui.layout

import com.radeusgd.archivum.languages.ViewLanguage

import scalafx.scene.layout.VBox

object VBoxFactory extends AggregateFactory(new VBox(_: _*) {
   spacing = 3
}) {
   override val nodeType: String = ViewLanguage.Vbox
}
