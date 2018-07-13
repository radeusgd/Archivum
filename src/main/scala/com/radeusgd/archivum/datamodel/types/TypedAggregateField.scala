package com.radeusgd.archivum.datamodel.types

trait TypedAggregateField {
   def getType(path: List[String]): FieldType
}
