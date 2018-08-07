package com.radeusgd.archivum.gui.controls

import com.radeusgd.archivum.datamodel.{DMStruct, DMValue, ValidationError}

trait BoundControl {
   def refreshBinding(newValue: DMStruct): Unit

   // TODO during early-dev allow controls to not handle errors, but later remove this
   def refreshErrors(errors: List[ValidationError]): List[ValidationError] = errors

   // by default don't do anything
   def augmentFreshValue(newValue: DMValue): DMValue = newValue
}