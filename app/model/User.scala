package model

import java.io._
import play.api.libs.json.Writes
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

case class User(login:String,password:String,email:String)




object User {
  
  implicit val userWrites: Writes[User] = new Writes[User] {
	def writes(o: User): JsValue = JsObject(
			"login" -> JsString(o.login) ::
			"email" -> JsString(o.email) 
			:: Nil
	    )
  }
  
  
  private lazy val OUTPUT_FILE = {
    val storeHouse = new java.io.File("users.csv")
    if(!storeHouse.exists()) storeHouse.createNewFile();
    storeHouse
  }
  
  var allUsers: Vector[User] = Vector()
	
  def findUserByLogin(login:String) : Option[User] = allUsers.find(_.login == login);
  def registerUser(u:User) = {
    allUsers = allUsers:+u 
    if(OUTPUT_FILE.exists()) OUTPUT_FILE.delete()
    val printWriter : PrintWriter = new PrintWriter(OUTPUT_FILE)
    printWriter.write("Login;Password;Email\n")
    allUsers.foreach{ u =>
      printWriter.write(u.login+";")
      printWriter.write(u.password+";")
      printWriter.write(u.email)
      printWriter.write("\n")
    }
    printWriter.close()
  }
  
  def init() {
    val file = scala.io.Source.fromFile(OUTPUT_FILE)
	file.getLines.drop(1).foreach {line =>
	 line.split(';').toList match {
	   case login::password::email::Nil => allUsers = allUsers :+ User(login,password,email)
	   case _ =>
	 }
	  
	}
  }
}