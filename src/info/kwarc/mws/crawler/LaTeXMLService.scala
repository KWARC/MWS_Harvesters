package info.kwarc.mws.crawler

import java.net._
import scala.xml._
import java.io._
import org.apache.commons._
import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client._
import java.util.ArrayList
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import spray.json._
import DefaultJsonProtocol._

class LaTeXMLService(url : URL) {
  def convert(profile : String, latex : String) : String = {
    val post = new HttpPost(url.toString());
    
    val nameValuePairs = new ArrayList[NameValuePair](1)
    nameValuePairs.add(new BasicNameValuePair("profile", profile))
    nameValuePairs.add(new BasicNameValuePair("tex", latex))
    
    post.setEntity(new UrlEncodedFormEntity(nameValuePairs))
    val client = new DefaultHttpClient()
    val response = client.execute(post)
    val entity = response.getEntity()
    val in = new BufferedReader(new InputStreamReader(entity.getContent()))
    var result = ""
    while(in.ready()) {
     result += in.readLine()
    }
    in.close()
    val json = JsonParser(result)
    val out = json.asJsObject.getFields("result") match {
      case Vector(JsString(math)) => math
      case _ => throw new java.lang.IllegalStateException("LATEXML deserialization error")
    }
    
    return out
  }
}