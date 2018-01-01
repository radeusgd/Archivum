package com.radeusgd.archivum.datamodel.types

trait FieldType {
   def createEmptyField(name: String): Field

}
