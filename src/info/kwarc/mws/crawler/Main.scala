package info.kwarc.mws.crawler
import java.io._

import java.net._

object Main {
  def main(args : Array[String]) {
    
      val url = new URL("http://latexml.mathweb.org/convert");
      val latexmlService = new LaTeXMLService(url);
      
      println(latexmlService.convert("math", "$$E=mc^2$$"));
    
  } 
}


object TestMain {
  def main(args : Array[String]) {
    val c = new GapXMLConverter()
    val start = new File("/var/data/localmh/MathHub/ODK/GAP/export/fromwebpage")
    val out = new File("/var/data/localmh/MathHub/ODK/GAP/export/mwscrawler")
    val ft = new FileTraverser(start, c)
    ft.apply(out)
  }
}