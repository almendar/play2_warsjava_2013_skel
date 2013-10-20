package controllers
import play.api.mvc.{Controller,Action}
import play.api.data.Forms._
import play.api.data.Form
import utils.Security
import model.Tweet
import model.User
import play.api.libs.json.Json
import model.Picture
import play.api.data.Forms
import utils.Diary
import play.api.libs.iteratee.Enumerator
import play.api.libs.concurrent.Promise
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.Comet
import play.api.mvc.WebSocket
import play.api.libs.json.JsValue
import play.api.libs.iteratee._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import org.joda.time.DateTime
import play.api.mvc.Results

object LoggedInUserActions extends Controller with Security {
  
  
  lazy val postImageForm : Form[(String)] = Form(
      Forms.mapping(
          "description" -> Forms.text
      )((desc) => desc)(desc => Some(desc))
  )
  
  
  lazy val tweetForm = Form(
        "tweetText" -> text(minLength=1, maxLength=160)
  )
  
  def postImage = IsAuthenticated{user => implicit request =>
    postImageForm.bindFromRequest.fold(
      errors => BadRequest(views.html.loggedin.uploadImage(postImageForm)),
      success => {
    	  val description = success
    	  val a = request.body.asMultipartFormData
    	  request.body.asMultipartFormData.get.file("picture").map { picture =>
    	    val acceptedMime = List("image/gif","image/jpeg","image/png")
    	  	val contentType = picture.contentType
    	  	contentType.map { ctype =>
    	  		Diary.logger.info(contentType.get)
    	  		val fileName = picture.filename
	    	  	if(acceptedMime.contains(ctype)) {
	    	  	  val newPath = new java.io.File(Picture.IMG_PATH + user + "_" + fileName)
	    	  	  picture.ref.moveTo(newPath)
	    	  	  val pictureData = Picture(fileName,user,description)
	    	  	  Picture.storePicture(pictureData)
	    	  	  Redirect(routes.LoggedInUserActions.showImages).flashing(("success","Uploaded"),("text","info"))
	    	  	}
	    	  	else Redirect(routes.LoggedInUserActions.showImages).flashing( ("success",s"Wrong mime type:${ctype}. Accepting only: ${acceptedMime.mkString(",")}"),("text","danger" ) )
  	    	  
    	    }.getOrElse(Redirect(routes.LoggedInUserActions.showImages).flashing(("success","Unknown mime type"),("text","danger")))

    	  }.getOrElse(Redirect(routes.LoggedInUserActions.showImages).flashing( ("success","No file was found in uploaded data"),("text","danger" ) ))

      }
    )
  }
  
  def showImages = IsAuthenticated{ user => implicit request =>
    val seqPic = Picture.getPicturesForUser(user)
  	Ok(views.html.loggedin.showImages(seqPic))
  }
  
  def serveImage(fileName:String) = IsAuthenticated{ user => implicit request =>
  	Ok.sendFile(new java.io.File(Picture.IMG_PATH + s"/${user}_" + fileName))
  }
  
 
  
  def imagePostPage = IsAuthenticated{user => implicit request =>
    Ok(views.html.loggedin.uploadImage(postImageForm))
  }
  
  
  def composeTweet = IsAuthenticated{ user => implicit request =>
    Ok(views.html.loggedin.composeTweet(tweetForm))
  }
  
  def postTweet = IsAuthenticated{user => implicit request =>
  	tweetForm.bindFromRequest.fold(
  	  errors => BadRequest(views.html.loggedin.composeTweet(tweetForm)),
  	  success => {
  	    val tweet : String = success
  	    Tweet.storeTweet(Tweet(user,tweet))
  	    Redirect(routes.LoggedInUserActions.composeTweet)
  	  }
  	)
  }
  
  def timeLine = IsAuthenticated{user => implicit request =>
  	implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isAfter _)
    val selectedTweets : Seq[Tweet] = Tweet.allTweets.filter{t => t.getMentions.contains("@"+user) || t.user==user}.sortBy { _.date } 
  	Ok(views.html.tweetList(selectedTweets))
  }
  
  def searchForUsersHtml = Action { implicit request =>
  	Ok(views.html.userSearch())
  }
  
  val s = Iterator.from(0);
  def cometStream = IsAuthenticated{user => implicit request => 
  	 val data : Enumerator[String] = Enumerator.fromCallback( () => 
      Promise.timeout(Some(s.next.toString+" " + user), 1000 milliseconds)
    );
  	 Ok.stream(data &> Comet(callback = "parent.cometMessage"))
  }
  
  def cometPage = IsAuthenticated{user => implicit request => 
    Ok(views.html.comet())
  }
  
  def seeMyPassword(user:String) = isAllowed(user) { user => request =>
    User.findUserByLogin(user).map{f => Ok(f.password)}.getOrElse(Results.NotFound)
  }
  
  
  def searchForUsers(name:String) = Action { request =>
    val logins = for {
      i <- User.allUsers if(i.login.startsWith(name))
    } yield i.login
    Ok(Json.toJson(logins))
  }
  
  
   var connected = Map.empty[String, Concurrent.Channel[JsValue]]
  
  
   def event = WebSocket.using[JsValue] {implicit request =>
   Diary.logger.info(s"Created websocket for user ${request.session.get("user").get}")
     request.session.get("user").map { user =>
     	val out = Concurrent.unicast[JsValue] { channel =>
     		connected = connected + (user->channel)
     	}
     	
     	val in = Iteratee.foreach[JsValue] { event =>
					play.Logger.info("received: " + event + " from " + user)
				}.mapDone { _ =>
					connected = connected - user
				}
				Diary.logger.info(s"In/out done")
		(in,out)
     }.getOrElse {
     		val  in = Done[JsValue,Unit]((),Input.EOF)
     		val out = Enumerator.eof[JsValue]
     		Diary.logger.info(s"In/out failed")
     		(in,out)
     }
   }
  
  
  
}