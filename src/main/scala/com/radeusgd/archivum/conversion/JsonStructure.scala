package com.radeusgd.archivum.conversion

import com.radeusgd.archivum.datamodel.Model
import spray.json.{JsArray, JsObject, JsValue}

object JsonStructure {
   def extract(js: JsValue): Structure = js match {
      case JsObject(fields) => Struct(fields.mapValues(extract))
      case JsArray(elements) => Array(elements.map(extract).fold(Leaf)(Structure.merge))
      case _ => Leaf
   }

   def unify(jsi: Iterable[JsValue]): Structure =
      jsi.map(extract).fold(Leaf)(Structure.merge)

   private def dropField(js: JsValue, path: Structure.Path): JsValue = path match {
      case Nil => throw new RuntimeException("Cannot drop empty path")
      case "*" :: t => js match {
         case JsArray(elems) => JsArray(elems.map(dropField(_, t)))
         case _ => throw new RuntimeException("Type mismatch (array expected)")
      }
      case field :: Nil => JsObject(js.asJsObject.fields - field)
      case field :: rest =>
         val fields = js.asJsObject.fields
         fields.get(field) match {
            case Some(v) => JsObject(fields.updated(field, dropField(v, rest)))
            case None => js
         }
   }

   def dropFields(paths: Seq[Structure.Path])(js: JsValue): JsValue =
      paths.foldLeft(js)(dropField)

   private def makeEmptyField(js: JsValue, path: Structure.Path, value: JsValue): JsValue = path match {
      case Nil => throw new RuntimeException("Empty path")
      case "*" :: rest => js match {
         case JsArray(elems) => JsArray(elems.map(makeEmptyField(_, rest, value)))
         case _ => throw new RuntimeException("Type mismatch (array expected)")
      }
      case f :: Nil =>
         val fields = js.asJsObject.fields
         JsObject(fields.updated(f, value))
      case f :: rest =>
         val fields = js.asJsObject.fields
         val inner = fields.getOrElse(f, JsObject())
         JsObject(fields.updated(f, makeEmptyField(inner, rest, value)))
   }

   def makeEmptyFields(model: Model, paths: Seq[Structure.Path])(js: JsValue): JsValue =
      paths.foldLeft(js)((j, path) => {
         val t = model.roottype.getType(path)
         val v = t.toHumanJson(t.makeEmpty)
         makeEmptyField(j, path, v)
      })
}
