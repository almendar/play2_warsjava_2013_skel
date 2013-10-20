package model

import org.specs2.Specification
import org.scalatest._

class TweetSpec extends FlatSpec {
  
  val tweets = List[Tweet](
    Tweet("user1","Some text tweet #funny"),
    Tweet("usser2","Mention someone @user1")
    
  )
  
  
  "Tweet nr 1" should "have a single hash tag #funny" in {
    assert(tweets(0).getTags.size === 1)
    assert(tweets(0).getTags(0) === "#funny")
  }
  
  
  "Tweet nr 2" should "have a single mention of user1" in {
    assert(tweets(1).getMentions.size === 1)
    assert(tweets(1).getMentions(0) === "@user1")

  } 

}