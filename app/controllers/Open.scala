package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import model.Tweet
import model.User

object Open extends Controller {
  
  def getUserTweetsAsAtom(user:String) = Action {implicit request =>
    val userTweets : Seq[Tweet] = Tweet.allTweets.filter(_.user == user)
    User.findUserByLogin(user).map( userFromDb =>
    	Ok(views.xml.open.tweetsFeed(userFromDb, userTweets)).as("application/atom+xml")
  ).getOrElse(BadRequest("User not found"))
  }

}