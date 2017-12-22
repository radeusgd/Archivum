package com.radeusgd.archivum.datamodel

class ModelDefinition(val name: String) {

}


import spray.json._

object ModelJsonProtocol extends DefaultJsonProtocol {
   implicit object ModelDefinitionJsonFormat extends RootJsonFormat[ModelDefinition] {
      override def write(obj: ModelDefinition): JsValue = JsObject() // TODO
      override def read(json: JsValue): ModelDefinition = {
         json.asJsObject.getFields("name") match {
            case Seq(JsString(name)) =>
               new ModelDefinition(name)
            case _ => throw DeserializationException("Wrong model root structure")
         }
      }
   }
  /*implicit object ColorJsonFormat extends RootJsonFormat[Color] {
    def write(c: Color) = JsObject(
      "name" -> JsString(c.name),
      "red" -> JsNumber(c.red),
      "green" -> JsNumber(c.green),
      "blue" -> JsNumber(c.blue)
    )
    def read(value: JsValue) = {
      value.asJsObject.getFields("name", "red", "green", "blue") match {
        case Seq(JsString(name), JsNumber(red), JsNumber(green), JsNumber(blue)) =>
          new Color(name, red.toInt, green.toInt, blue.toInt)
        case _ => throw new DeserializationException("Color expected")
      }
    }
  }*/
}

/*
implicit object MoneyFormat extends JsonFormat[Money] {
  val fmt = """([A-Z]{3}) ([0-9.]+)""".r
  def write(m: Money) = JsString(s"${m.currency} ${m.amount}")
  def read(json: JsValue) = json match {
    case JsString(fmt(c, a)) => Money(c, BigDecimal(a))
    case _ => deserializationError("String expected")
  }
}
 */