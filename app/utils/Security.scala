package utils

import play.api.mvc.RequestHeader
import play.api.mvc.{Results,Request,Result,Action}
import controllers.routes
import play.api.mvc.AnyContent
import play.api.mvc.BodyParser
import play.api.mvc.BodyParsers

trait Security {
  
  private def userIdentity(headers : RequestHeader) = headers.session.get("user")
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.SignUp.login)
  
  
/** 
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = play.api.mvc.Security.Authenticated(userIdentity, onUnauthorized) { user =>
    Action(request => f(user)(request))
  }
  
  
  def isAllowed(userName:String)(f: => String => Request[AnyContent] => Result) = IsAuthenticated{ user => request =>
  	if(userName == user)
  		f(user)(request)
  	  else 
  	    Results.Forbidden
  }
  
  
  
  
}