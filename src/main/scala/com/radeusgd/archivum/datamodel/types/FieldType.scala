package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel._

trait FieldType {
   def validate(v: DMValue): List[ValidationError]

   def makeEmpty: DMValue
}

object StringField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMString(_) => Nil
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMString") :: Nil
      }

   override def makeEmpty: DMValue = DMString("")
}

object IntegerField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMNull => Nil
         case DMInteger(_) => Nil
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMInteger") :: Nil
      }

   override def makeEmpty: DMValue = DMNull
}

object DateField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMNull => Nil
         case DMDate(_) => Nil
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMDate") :: Nil
      }

   override def makeEmpty: DMValue = DMNull
}

object YearDateField extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMNull => Nil
         case DMYearDate(_) => Nil
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMYearDate") :: Nil
      }

   override def makeEmpty: DMValue = DMNull
}

// TODO computable fields
case class StructField(fieldTypes: Map[String, FieldType]) extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMStruct(values, _) =>
            val unknown = values.keySet -- fieldTypes.keySet
            val unknownErrors: List[ValidationError] = unknown.toList map { key => ConstraintError(Nil, key + " is not expected in this struct") }

            val childErrors: List[ValidationError] =
               fieldTypes.toList flatMap { case (name, ft) =>
                  values.get(name) match {
                     case Some(vv) => ft.validate(vv) map {
                        _.extendPath(name)
                     }
                     case None => ConstraintError(Nil, name + " is missing") :: Nil
                  }
               }

            unknownErrors ++ childErrors.toList
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMStruct") :: Nil
      }

   def getType(path: List[String]): FieldType = // TODO option/either
      path match {
         case Nil => this
         case last :: Nil => fieldTypes(last)
         case next :: rest => fieldTypes(next).asInstanceOf[StructField].getType(rest) // TODO FIXME add array support and error checking
      }

   override def makeEmpty: DMStruct = DMStruct(fieldTypes mapValues (_.makeEmpty), Map.empty)
}

case class ArrayField(elementsType: FieldType) extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMArray(values) => {
            val indexedValues: Seq[(DMValue, Int)] = values.zipWithIndex
            val childErrors: Seq[ValidationError] =
               indexedValues flatMap { case (vv, ind) =>
                  elementsType.validate(vv) map {
                     _.extendPath(ind.toString)
                  }
               }

            childErrors.toList
         }
         case _ => TypeError(Nil, v.getClass.getSimpleName, "DMArray") :: Nil
      }

   override def makeEmpty: DMArray = DMArray(Vector.empty)
}

// TODO Image Field (uses DMString for content address and manual content handling)
