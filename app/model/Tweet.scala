package model

import org.joda.time.DateTime
import java.io.PrintWriter
import play.api.libs.json.Writes
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.Json
import utils.Diary
import play.api.libs.json.Reads
import play.api.libs.json.JsObject
import play.api.libs.json.JsResult


case class Tweet(user:String,text:String, date:DateTime = new DateTime) {
  
   

  private def getWordsStartingWithAChar(c:Char) : List[String] = {
    val regex = ("(?<=^|(?<=[^a-zA-Z0-9-_\\.]))"+c+"([A-Za-z]+[A-Za-z0-9]+)").r
    regex.findAllIn(text).toList
  }
  def getMentions = getWordsStartingWithAChar('@')
  def getTags = getWordsStartingWithAChar('#')
  
}


object Tweet {
  
 implicit val tweetWrites: Writes[Tweet] = new Writes[Tweet] {
	def writes(o: Tweet): JsValue = Json.obj(
			"login" -> o.user,
			"text" -> o.text,
			"date" -> (o.date) 
	    )
  }
 
 implicit val tweetReads: Reads[Tweet] = new Reads[Tweet] {
   def reads(json: JsValue) : JsResult[Tweet] = 
     (json \ "login").validate[String].flatMap {login =>
       (json \ "text").validate[String].flatMap {text =>
         (json \ "date").validate[DateTime].map{date =>
           Tweet(login,text,date)
           }
         }
      }
     
     
   
   
         
         
   }
 
  
  private val OUTPUT_FILE = new java.io.File("tweets.csv")
  if(!OUTPUT_FILE.exists()) OUTPUT_FILE.createNewFile();
  
  var allTweets = Vector[Tweet]()
  
  
  def storeTweet(t:Tweet) = {
	  allTweets = allTweets :+ t
	  if(OUTPUT_FILE.exists()) OUTPUT_FILE.delete()
	  val printWriter : PrintWriter = new PrintWriter(OUTPUT_FILE)
	  printWriter.write("User;Text;Date\n")
	  allTweets.foreach{ u =>
      printWriter.write(u.user+";")
      printWriter.write(u.text+";")
      printWriter.write(u.date.toString())
      printWriter.write("\n")
    }
    printWriter.close()
    Diary.logger.info(s"Storing tweet for ${t.user}. Connected users: ${controllers.LoggedInUserActions.connected.toString}")
    t.getMentions.map(_.drop(1)).foreach { mentionedUser => 
          controllers.LoggedInUserActions.connected.find{  p=>  p._1 == mentionedUser }.map { case(user,channel) =>
      Diary.logger.info(s"Pushing message to ${mentionedUser}")
      channel.push(Json.toJson(t))
    }
    }

  }
  
  
  def init() {
    val file = scala.io.Source.fromFile(OUTPUT_FILE)
	file.getLines.drop(1).foreach {line =>
	 line.split(';').toList match {
	   case user::text::date::Nil => allTweets = allTweets :+ Tweet(user,text,new DateTime(date))
	   case _ =>
	 }
	  
	}
  }
  
}