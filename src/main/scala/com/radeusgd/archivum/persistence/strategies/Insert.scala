package com.radeusgd.archivum.persistence.strategies

trait Insert {
   def setValue(path: Seq[String], value: Any): Unit // TODO
   def setSubTable(path: Seq[String], amount: Int): Seq[Insert]
}

class InsertImpl extends Insert {
   // TODO
   override def setValue(path: Seq[String], value: Any): Unit = ???

   override def setSubTable(path: Seq[String], amount: Int): Seq[Insert] = ???
}