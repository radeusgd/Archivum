package com.radeusgd.archivum.persistence.impl.h2scalikejdbc

import com.radeusgd.archivum.datamodel.Model
import com.radeusgd.archivum.persistence.DBUtils.sanitizeName
import com.radeusgd.archivum.persistence.{Database, Repository}
import scalikejdbc._

/* TODO if there are multiple backends this will diverge into various implementations
   However this won't be very easy because in the beginning
   I assume FieldType will handle their serialization each on their own,
   so if later rewriting for multiple backends that will need to be refactored into some kind of TypeSerializer[FieldType]
 */
class DatabaseImpl(val db: DB) extends Database {
   ensureModelTable()

   override def openRepository(modelName: String): Option[Repository] = {
      for {
         model <- getRepositoryModel(modelName)
      } yield new RepositoryImpl(model, sanitizeName(modelName), db)
   }

   // TODO probably better use model instance: TODO model.toJson (implement Writer)
   override def createRepository(modelDefinition: String): Unit = {
      val model = Model.fromDefinition(modelDefinition).get // TODO this may not be the best error handling in the world
      val setup: SetupImpl = new SetupImpl(sanitizeName(model.name))
      model.roottype.tableSetup(Nil, setup)
      val modelCreation = setup.createSchema()
      db.localTx { implicit session =>
         sql"INSERT INTO models (name, definition) VALUES(${model.name}, $modelDefinition)".update.apply()
         modelCreation.foreach(_.update.apply())
      }
   }

   private def getRepositoryModel(name: String): Option[Model] = {
      val definition: Option[String] =
         db.readOnly { implicit session =>
            sql"SELECT definition FROM models WHERE name = $name"
               .map(rs => rs.string("definition")).single.apply()
         }
      definition.flatMap(Model.fromDefinition(_).toOption)
   }

   private def ensureModelTable(): Unit = {
      db.localTx { implicit session =>
         sql"""CREATE TABLE IF NOT EXISTS models (
                  name VARCHAR(100) PRIMARY KEY,
                  definition VARCHAR(9000)
                  )""".execute.apply()
      }
   }

   override def listRepositories(): Seq[String] = {
      db.readOnly { implicit session =>
         sql"SELECT name FROM models"
            .map(rs => rs.string("name")).list.apply()
      }
   }
}
