package com.radeusgd.archivum.persistence.impl.h2scalikejdbc

import com.radeusgd.archivum.persistence.RidSetHelper
import scalikejdbc._

class RidSetHelperImpl(val db: DB, val table: SQLSyntax) extends RidSetHelper {
   override def count(): Rid =
      db.readOnly({ implicit session =>
         sql"SELECT COUNT(_rid) FROM $table"
            .map(rs => rs.long(1)).single.apply()
      }).get // count should always return a value

   override def getTemporaryIndex(rid: Rid): Rid =
      db.readOnly({ implicit session =>
         sql"SELECT COUNT(_rid) FROM $table WHERE _rid <= $rid"
            .map(rs => rs.long(1)).single.apply()
      }).get

   override def getCloseRid(rid: Rid): Option[Rid] =
      db.readOnly({ implicit session =>
         sql"SELECT _rid FROM $table WHERE _rid < $rid ORDER BY _rid DESC LIMIT 1"
            .map(rs => rs.long("_rid")).single.apply()
      }).orElse(
         db.readOnly({ implicit session =>
            sql"SELECT _rid FROM $table WHERE _rid > $rid ORDER BY _rid ASC LIMIT 1"
               .map(rs => rs.long("_rid")).single.apply()
         })
      )

   override def fetchAllIds(): Seq[Rid] =
      db.readOnly({ implicit session =>
         sql"SELECT _rid FROM $table"
            .map(rs => rs.long("_rid")).list.apply()
      })

   override def getFirstRid(): Option[Rid] =
      db.readOnly({ implicit session =>
         sql"SELECT _rid FROM $table ORDER BY _rid ASC LIMIT 1"
            .map(rs => rs.long("_rid")).single.apply()
      })

   override def getLastRid(): Option[Rid] =
      db.readOnly({ implicit session =>
         sql"SELECT _rid FROM $table ORDER BY _rid DESC LIMIT 1"
            .map(rs => rs.long("_rid")).single.apply()
      })

   override def getNextRid(rid: Rid): Option[Rid] =
      db.readOnly({ implicit session =>
         sql"SELECT _rid FROM $table WHERE _rid > $rid ORDER BY _rid ASC LIMIT 1"
            .map(rs => rs.long("_rid")).single.apply()
      })

   override def getPreviousRid(rid: Rid): Option[Rid] =
      db.readOnly({ implicit session =>
         sql"SELECT _rid FROM $table WHERE _rid < $rid ORDER BY _rid DESC LIMIT 1"
            .map(rs => rs.long("_rid")).single.apply()
      })
}
