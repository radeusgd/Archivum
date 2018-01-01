package com.radeusgd.archivum.datamodel.types

import javafx.scene.layout.Pane

trait Field {
   /*
   Sets the value if possible.
   If the value is not suitable for the underlying format returns false and doesn't change the value.
    */
   def set(value: String): Boolean
   def get: String
   def getName: String

  override def toString: String = get
}
