package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.JsValue
import play.api.libs.iteratee.Enumerator
import scala.concurrent.Future
import java.io.InputStream
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.Stream
import play.api.libs.concurrent.Promise
import scala.concurrent.duration._
import java.io.ByteArrayInputStream
import play.api.libs.iteratee.Enumeratee
import play.api.http.Writeable
import play.api.http.ContentTypeOf

object Application extends Controller {
  
  val s = Iterator.from(0);
  
  def index : Action[AnyContent] = Action { implicit request =>
    Ok(views.html.index("Your new application is ready."))
  }
  
  def jaja = Action {
    Ok(widoki.html.dlaJaj())
  }
  
  
  val storeInUserFile = parse.using { request =>
    request.session.get("username").map { user =>
    // parse.file(to = new java.io.File("d:\\" + user + ".upload"))
     parse.temporaryFile
    }.getOrElse {
      parse.error(Unauthorized("You don't have the right to upload here"))
    }
  }
  
  def saveToFile = Action(parse.tolerantText) { request =>
    Redirect(routes.RegexFun.twoParameters(request.body,""))
    
  }
  
  def takeOverTheWorld = TODO
  

  def js = Action { implicit request => 
    import routes.javascript._
   Ok( Routes.javascriptRouter("_playRouter")(controllers.routes.javascript.LoggedInUserActions.searchForUsers)
     ).as(JAVASCRIPT)
  }
  
  /* unknwon size */
  def steadyStream = Action{ implicit request =>
 
    val data : Enumerator[String] = Enumerator.fromCallback( () => 
      Promise.timeout(Some(s.next.toString+" "), 1000 milliseconds)
    );
    Ok.stream(data);
  }
  
  
  def steadyStreamKnownSize = Action { implicit request =>
    implicit def contentTypeOf_ArrayofString(implicit codec: Codec) : ContentTypeOf[Array[String]] = {
      ContentTypeOf[Array[String]](Some("text/plain"))
    }
    implicit def wAString(implicit codec:Codec) : Writeable[Array[String]] = Writeable( array  => codec.encode(array.mkString("\n")) ) 
    val bytes : Array[Byte] = (1 to 255).toArray.map(x=> x.toByte)
    val data : ByteArrayInputStream  = new ByteArrayInputStream(bytes)
  	val dataEnum : Enumerator[Array[Byte]] = Enumerator.fromStream(data)
  	val byteToString: Enumeratee[Array[Byte],Array[String]] = Enumeratee.map[Array[Byte]]{ s => s.map(x=>x.toString) }
    val transormedData : Enumerator[Array[String]] = dataEnum.through(byteToString)
  	SimpleResult(
      ResponseHeader(200, Map(CONTENT_LENGTH -> bytes.length.toString)),
  	  body = transormedData//byteToString.transform(dataEnum)
  	)
  }
}