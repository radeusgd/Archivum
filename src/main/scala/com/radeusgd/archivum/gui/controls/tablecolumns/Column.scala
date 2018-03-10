package com.radeusgd.archivum.gui.controls.tablecolumns

import com.radeusgd.archivum.datamodel.DMValue
import com.radeusgd.archivum.gui.controls.BoundControl

import scalafx.scene.control.TableColumn

abstract class Column[A] extends TableColumn[DMValue, A] {
   //def refresh(): Unit // TODO
}