package com.radeusgd.archivum.gui.layout

import com.radeusgd.archivum.gui.controls.BoundControl

import scalafx.scene

case class ParsedLayout(node: scene.Node, boundControls: Seq[BoundControl])
