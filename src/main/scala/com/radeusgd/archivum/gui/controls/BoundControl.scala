package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel.{DMStruct, ValidationError}

trait BoundControl {
   def refreshBinding(newValue: DMStruct): Unit

   // TODO during early-dev allow controls to not handle errors, but later remove this
   def refreshErrors(errors: List[ValidationError]): List[ValidationError] = errors
}