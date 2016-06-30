package info.kwarc.mws.crawler

import java.net._

object Main {
  def main(args : Array[String]) {
    
      val url = new URL("http://latexml.mathweb.org/convert");
      val latexmlService = new LaTeXMLService(url);
      
      println(latexmlService.convert("12", "$$E=mc^2$$"));
    
  }
  
}