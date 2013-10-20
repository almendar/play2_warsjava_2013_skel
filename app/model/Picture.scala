package model
import java.io.File
import java.io.PrintWriter

case class Picture(fileName:String, owner:String, description:String) 

object Picture {
  
  val IMG_PATH = "img/"
    
  def getPicturesForUser(userName:String) : Seq[Picture] = allPicutres.filter{pic => pic.owner == userName} 
  
  
  var allPicutres : Seq[Picture] = Vector()
  
  private lazy val OUTPUT_FILE = {
    val storeHouse = new java.io.File("pictures.csv")
    if(!storeHouse.exists()) storeHouse.createNewFile();
    storeHouse
  }
  
  def storePicture(u:Picture) = {
    allPicutres = allPicutres:+u 
    if(OUTPUT_FILE.exists()) OUTPUT_FILE.delete()
    val printWriter : PrintWriter = new PrintWriter(OUTPUT_FILE)
    printWriter.write("File name;Owner;Description\n")
    allPicutres.foreach{ u =>
      printWriter.write(u.fileName+";")
      printWriter.write(u.owner+";")
      printWriter.write(u.description)
      printWriter.write("\n")
    }
    printWriter.close()
  }
  
  def init() {
    val file = scala.io.Source.fromFile(OUTPUT_FILE)
	file.getLines.drop(1).foreach {line =>
	 line.split(';').toList match {
	   case fileName::owner::desc::Nil => allPicutres = allPicutres :+ Picture(fileName,owner,desc)
	   case _ =>
	 }
	  
	}
  }
  
}