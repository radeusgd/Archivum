package com.radeusgd.archivum.gui.controls.tablecolumns

import com.radeusgd.archivum.gui.controls._
import com.radeusgd.archivum.languages.ViewLanguage

object TextColumnFactory
   extends CommonColumnFactory(ViewLanguage.TextColumn, new TextControl(_, _, _))

object ChoiceColumnFactory
   extends CommonColumnFactory(ViewLanguage.ChoiceColumn, new ChoiceControl(_, _, _))

object ClassicDateColumnFactory
   extends CommonColumnFactory(ViewLanguage.ClassicDateColumn, new ClassicDateControl(_, _, _))

object DateColumnFactory
   extends CommonColumnFactory(ViewLanguage.DateColumn, new DateControl(_, _, _))


object YearDateColumnFactory
   extends CommonColumnFactory(ViewLanguage.YearDateColumn, new YearDateControl(_, _, _))

object IntegerColumnFactory
   extends CommonColumnFactory(ViewLanguage.IntegerColumn, new IntegerControl(_, _, _))

object HackyImageColumnFactory
   extends SimpleColumnFactory(ViewLanguage.ImageColumn, new HackyImageControl(_, _, _))