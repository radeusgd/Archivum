package com.radeusgd.archivum.conversion

import com.radeusgd.archivum.datamodel.Model
import com.radeusgd.archivum.datamodel.types.{ArrayField, FieldType, StructField}

object ModelStructure {
   def extract(model: Model): Structure = extract(model.roottype)

   def extract(ft: FieldType): Structure = ft match {
      case StructField(fields) => Struct(fields.mapValues(extract))
      case ArrayField(elemt) => Array(extract(elemt))
      case _ => Leaf
   }
}
