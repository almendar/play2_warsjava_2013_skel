package utils

import play.api.Logger
object Diary {
  
  val logger = Logger("events")
  
  def storeEvent(msg:String) = logger.info(msg);
  
  def storeProblem(msg:String="",t:Throwable) = logger.error(msg, t)

}