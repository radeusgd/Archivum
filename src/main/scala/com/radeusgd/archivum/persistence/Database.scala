package com.radeusgd.archivum.persistence

import com.radeusgd.archivum.datamodel.Model

trait Database {
   def openRepository(modelName: String): Option[Repository]
   def createRepository(model: Model)
}

/* TODO if there are multiple backends this will diverge into various implementations
   However this won't be very easy because in the beginning
   I assume FieldType will handle their serialization each on their own,
   so if later rewriting for multiple backends that will need to be refactored into some kind of TypeSerializer[FieldType]
 */
class DatabaseImpl extends Database {
   override def openRepository(modelName: String): Option[Repository] = ???

   override def createRepository(model: Model): Unit = ???
}

object Database {
   def open(): Database = {
      ???
   }
}