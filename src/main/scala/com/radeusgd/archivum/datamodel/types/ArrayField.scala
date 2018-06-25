package com.radeusgd.archivum.datamodel.types

import com.radeusgd.archivum.datamodel.{DMArray, DMValue, TypeError, ValidationError}
import com.radeusgd.archivum.persistence.strategies.{Fetch, Insert, Setup}
import spray.json.{DeserializationException, JsArray, JsValue}
import cats.implicits._

case class ArrayField(elementsType: FieldType) extends FieldType {
   def validate(v: DMValue): List[ValidationError] =
      v match {
         case DMArray(values) =>
            val indexedValues: Seq[(DMValue, Int)] = values.zipWithIndex
            val childErrors: Seq[ValidationError] =
               indexedValues.flatMap { case (vv, ind) =>
                  elementsType.validate(vv).map(_.extendPath(ind.toString))
               }

            childErrors.toList
         case _ => TypeError(Nil, v.toString, "DMArray") :: Nil
      }

   override def makeEmpty: DMArray = DMArray(Vector.empty)

   override def tableSetup(path: Seq[String], table: Setup): Unit = {
      val sub = table.addSubTable(path)
      elementsType.tableSetup(Nil, sub)
   }

   override def tableFetch(path: Seq[String], table: Fetch): DMValue =
      DMArray(table.getSubTable(path, sub => elementsType.tableFetch(Nil, sub)).toVector)

   override def tableInsert(path: Seq[String], table: Insert, value: DMValue): Unit = {
      val arr: DMArray = value.asInstanceOf[DMArray]
      val subs = table.setSubTable(path, arr.length)
      val vAndSub: Seq[(Insert, DMValue)] = subs.zip(arr.values)
      vAndSub.foreach({ case (sub, svalue) => elementsType.tableInsert(Nil, sub, svalue) })
   }

   override def toHumanJson(v: DMValue): JsValue = {
      val arr = v.asInstanceOf[DMArray]
      JsArray(arr.values.map(toHumanJson))
   }

   override def fromHumanJson(j: JsValue): Either[Throwable, DMValue] = j match {
      case JsArray(elements) =>
         val values: Vector[Either[Throwable, DMValue]] = elements.map(elementsType.fromHumanJson)
         type EitherThrowable[A] = Either[Throwable, A]
         values.sequence[EitherThrowable, DMValue]
            .map(DMArray)
      case _ => Left(DeserializationException("Expected an object"))
   }
}
