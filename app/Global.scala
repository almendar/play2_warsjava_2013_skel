import model.User
import play.api.GlobalSettings
import play.Logger
import play.api._
import model.Tweet
import model.Picture

object Global extends GlobalSettings {
  
 override def onStart(app: Application) {
     User.init
     Tweet.init
     Picture.init
  }
}