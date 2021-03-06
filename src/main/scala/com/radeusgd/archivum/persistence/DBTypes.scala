package com.radeusgd.archivum.persistence

object DBTypes {

   sealed trait DBType

   object Integer extends DBType

   object String extends DBType

   case class ShortString(length: Int) extends DBType

   val Rid: DBType = Integer
}
