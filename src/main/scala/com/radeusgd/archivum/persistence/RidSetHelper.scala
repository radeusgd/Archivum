package com.radeusgd.archivum.persistence

trait RidSetHelper {
   type Rid = Long // TODO tie this Rid to Repository.Rid

   /*
   Returns amount of records in Repository.
    */
   def count(): Long

   /*
   Returns the index - amount of records before the given one (inclusive).
    */
   def getTemporaryIndex(rid: Rid): Long

   /*
   Intended to be used after delete to find record to display.
   It returns the closest rid before the given one or if there's no such rid, the closest rid after it.
   It returns None only if the Repository is empty or the provided rid is the only element in it.
    */
   def getCloseRid(rid: Rid): Option[Rid]

   /*
   Returns a list of all rids existing in the Repository.
   (Warning: result may be huge.)
    */
   def fetchAllIds(): Seq[Rid]

   def getFirstRid(): Option[Rid]

   def getLastRid(): Option[Rid]

   def getNextRid(rid: Rid): Option[Rid]

   def getPreviousRid(rid: Rid): Option[Rid]
}
