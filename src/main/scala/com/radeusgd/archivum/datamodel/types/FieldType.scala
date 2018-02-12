package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel._

trait FieldType {
   def validate(v: DMValue): List[ValidationError]
}

object StringField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMString(_) => Nil
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMString") :: Nil
      }
}

object IntegerField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMInteger(_) => Nil
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMInteger") :: Nil
      }
}

object DateField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         //case DMInteger(_) => Nil
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMDate TODO") :: Nil
      }
}

case class StructField(fieldTypes: Map[String, FieldType]) extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMStruct(values) => {
            val unknown = values.keySet -- fieldTypes.keySet
            val unknownErrors: List[ValidationError] = unknown.toList map { key => ConstraintError(Nil, key + " is not expected in this struct") }

            val childErrors: List[List[ValidationError]] =
               fieldTypes.toList map { case (name, ft) =>
                  values.get(name) match {
                     case Some(vv) => ft.validate(vv) map {
                        _.extendPath(name)
                     }
                     case None => ConstraintError(Nil, name + " is missing") :: Nil
                  }
               }

            unknownErrors ++ childErrors.flatten.toList
         }
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMStruct") :: Nil
      }
}

case class ArrayField(elementsType: FieldType) extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMArray(values) => {
            val indexedValues: Seq[(DMValue, Int)] = values.zipWithIndex
            val childErrors: Seq[List[ValidationError]] =
               indexedValues map { case (vv, ind) =>
                  elementsType.validate(vv) map {
                     _.extendPath("[" + ind + "]")
                  }
               }

            childErrors.flatten.toList
         }
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMArray") :: Nil
      }
}