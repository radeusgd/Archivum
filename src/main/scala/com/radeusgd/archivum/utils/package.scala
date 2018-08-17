package com.radeusgd.archivum

import scala.annotation.tailrec

package object utils {
   def splitList[T](list: List[T], on: T): List[List[T]] = {
      @tailrec
      def helper(inputList: List[T], accList: List[T], res: List[List[T]]): List[List[T]] =
         inputList match {
            case Nil => accList.reverse :: res
            case head :: tail =>
               if (head == on) helper(tail, Nil, accList.reverse :: res)
               else helper(tail, head :: accList, res)
         }
      helper(list, Nil, Nil).reverse
   }

}
