package com.radeusgd.archivum.gui.controls.commonproperties

import com.radeusgd.archivum.datamodel.DMValue
import com.radeusgd.archivum.gui.controls.BoundControl

trait SetValueFromOldOne {
   def setValueFromOldOne(v: DMValue): DMValue
}

trait PreviosValueOnCreation extends BoundControl with HasCommonProperties with SetValueFromOldOne {
   override def augmentFreshValue(newValue: DMValue): DMValue = {
      val v = super.augmentFreshValue(newValue)
      if (commonProperties.sticky) setValueFromOldOne(v)
      else v
   }
}
