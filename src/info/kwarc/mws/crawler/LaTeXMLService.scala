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

class LaTeXMLService(url : URL) {
  def convert(profile : String, latex : String) : String = {
    
    val post = new HttpPost(url.toString());
    
    val nameValuePairs = new ArrayList[NameValuePair](1)
    nameValuePairs.add(new BasicNameValuePair("profile", profile));
    nameValuePairs.add(new BasicNameValuePair("tex", latex));
    
    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));    
    
    val client = new DefaultHttpClient(); 
    
    println(post.getRequestLine());
    
    val response = client.execute(post);
    
    println(response.getEntity());
    
    return response.toString();
  }
}