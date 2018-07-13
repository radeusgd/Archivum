package com.radeusgd.archivum.gui.controls.tablecolumns

import com.radeusgd.archivum.gui.controls._
import com.radeusgd.archivum.languages.ViewLanguage

object TextColumnFactory
   extends SimpleColumnFactory(ViewLanguage.TextColumn, new TextControl(_, _, _))

object ChoiceColumnFactory
   extends SimpleColumnFactory(ViewLanguage.ChoiceColumn, new ChoiceControl(_, _, _))

object ClassicDateColumnFactory
   extends SimpleColumnFactory(ViewLanguage.ClassicDateColumn, new ClassicDateControl(_, _, _))

object DateColumnFactory
   extends SimpleColumnFactory(ViewLanguage.DateColumn, new DateControl(_, _, _))

object IntegerColumnFactory
   extends SimpleColumnFactory(ViewLanguage.IntegerColumn, new IntegerControl(_, _, _))