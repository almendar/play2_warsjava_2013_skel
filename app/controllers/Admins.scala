package controllers

import play.api.mvc.Controller
import play.api.mvc.Action

object Admins extends Controller{
 
  
  def allUsers = Action { implicit request =>
    Ok(views.html.registration.allusers())
  }
  
  
  def seeAllTweets = Action { implicit request =>
    Ok(views.html.publicTimeLine())
  }


}