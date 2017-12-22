package com.radeusgd.archivum.datamodel.types

trait Field {
   /*
   Sets the value if possible.
   If the value is not suitable for the underlying format returns false and doesn't change the value.
    */
   def set(value: String): Boolean
   def get: String

  override def toString: String = get
}
