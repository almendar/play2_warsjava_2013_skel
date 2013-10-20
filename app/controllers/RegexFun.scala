package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.mvc.Cookie
import play.api.mvc.Cookie

object RegexFun extends Controller {
  
  def twoParameters(iden1:String,iden2:String) = Action {
     Ok(s"${iden1} ${iden2}")
  }
  
  def numberEndingWithS(iden:String) = Action {
    Ok(s"${iden}").withCookies(Cookie("ble ble", "ble ble"))
  }
  
   def plusPlus(iden:Long) = Action {
    Ok(s"${iden+1}")
  }
   
   def pureStringParam(param:String) = Action {
     Ok(s"${param}")
   }
   
  def stringLongParam(param1:String,param2 : Long) = Action {
     Ok(s"${param1} ${param2}")
   }

}