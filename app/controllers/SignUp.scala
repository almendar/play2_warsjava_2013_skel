package controllers


import model.User
import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms
import play.api.mvc.Action
import play.api.Logger
import utils.Diary

object SignUp extends Controller {
  
  val signupForm : Form[User] = Form(
      Forms.mapping(
          "login" -> Forms.text(minLength = 4),
          
          "password"->Forms.tuple(
              "main"->Forms.nonEmptyText(5, 80),
              "confirm"->Forms.text).verifying("Passwords does not match", p => p._1==p._2),
         
          "email"->Forms.email
          )((login,password,email)=> User(login,password._1,email))(user=>Some(user.login,(user.password,""),user.email))
  )
  
  
  val loginForm : Form[Tuple2[String,String]] = Form(
		  Forms.tuple(
		      "login" -> Forms.text(minLength = 4),
		      "password" -> Forms.nonEmptyText(5,80)
		      )
  )
  
  def tryToAuthenticate = Action { implicit request =>
     loginForm.bindFromRequest.fold(
         errors => BadRequest(views.html.login(errors)),
         success => {
           val (user,password) = success
          if(User.allUsers.filter{u => u.login==user && u.password==password}.isEmpty) { 
        	  Diary.storeEvent(s"Unsuccessfull login of ${user}")
              Redirect(routes.SignUp.login).flashing("success" -> "Please try again")
          }
          else {
               Diary.storeEvent(s"Successful login of ${user}")
               Redirect(routes.Admins.allUsers).withSession("user"->user)
          }
        }
     )
  }
  
  def login = Action { implicit request => 
    Ok(views.html.login(loginForm))
  }
  
  def signUpPage = Action { implicit request =>
    Ok(views.html.signup(signupForm))
  }
  
  def submit = Action { implicit request =>
    signupForm.bindFromRequest.fold(
      // Form has errors, redisplay it
      errors => BadRequest(views.html.signup(errors)),
      
      // We got a valid User value, display the summary
      user => {
        User.registerUser(user)
        Ok(views.html.registration.allusers())
      }
    )
  }
  
  
  def logout = Action { implicit request =>
    Diary.storeEvent(s"Logout ${request.session.get("user").getOrElse("Unknown")}")
    Redirect(routes.SignUp.login).withNewSession.flashing("success"->"Wylogowano")
  }
  
  
  
  
  
  

}