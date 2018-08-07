package com.radeusgd.archivum.gui.controls.commonproperties

import com.radeusgd.archivum.datamodel.{DMStruct, DMValue}
import com.radeusgd.archivum.gui.controls.BoundControl

trait SetDefaultValueFromString {
   def setDefaultFromString(v: DMValue, s: String): DMValue
}

trait DefaultValueOnCreation extends BoundControl with HasCommonProperties with SetDefaultValueFromString {
   override def augmentFreshValue(newValue: DMValue): DMValue = {
      val v = super.augmentFreshValue(newValue)
      commonProperties.default match {
         case Some(defaultValue) => setDefaultFromString(v, defaultValue)
         case None => v
      }
   }
}
