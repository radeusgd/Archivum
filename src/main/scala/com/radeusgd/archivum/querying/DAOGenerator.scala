package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel.Model
import com.radeusgd.archivum.datamodel.types.{ArrayField, FieldType, StringField, StructField}

object DAOGenerator {
   private def sanitize(str: String): String = {
      val cleaned = str.replaceAll("[^\\p{L}]", "_")
      cleaned.head.toLower + cleaned.tail
   }

   def makeDAO(model: Model): String = makeDAOClass(model.name, model.roottype)

   def makeDAOClass(name: String, defn: StructField): String = {
      val props = defn.fieldTypes.toList.map((makeDAOForType _).tupled)
      val sanitized = sanitize(name)
      val className = sanitized.head.toUpper + sanitized.tail
      s"class $className (value: DMStruct) {" +
         "\n" + props.mkString("\n") + "\n}"
   }

   def makeDAOForType(name: String, defn: FieldType): String =
      defn match {
         case sf: StructField =>
            val sanitized = sanitize(name)
            val className = sanitized.head.toUpper + sanitized.tail
            makeDAOClass(name, sf) +
               s"\nval $sanitized: $className = new " + className + "(value(\"" + name + "\").asInstanceOf[DMStruct])"
         case StringField =>
            s"val " + sanitize(name) + ": String = value(\"" + name + "\").asString.get"
         case ArrayField(elementsType) => s"val ${sanitize(name)} = Seq()" // TODO
         case _ => s"val ${sanitize(name)} = null" // TODO
      }

}