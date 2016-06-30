package info.kwarc.mws.crawler

import java.io._
import scala.xml._

abstract class Converter {
  def inFormat : String
  def outFormat : String
  def apply(in : Node) : List[Node]
}

abstract class BaseConverter(val inFormat : String, val outFormat : String) extends Converter

class ComposedConverter(head : Converter, tail : Converter) extends Converter {
  require(head.outFormat == tail.inFormat, "cannot compose Converter, formats don't match")
  def inFormat = head.inFormat
  def outFormat = tail.outFormat
  def apply(in : Node) = head(in).flatMap(tail.apply)
}

class GapXMLConverter(inFormat : String, outFormat : String) extends BaseConverter(inFormat, outFormat) {
  def apply(n : Node) : List[Node] = {
    var snippets : List[Node] = Nil
    n.child foreach {
      case c if c.label == "p" && (c \ "@id").text == ??? => ???
      case _ => ??? /// recurse
    }
    snippets
  } 
}


abstract class Traverser(converter : Converter) {
  def apply(outFolder : File) : Unit
  def writeOut(outFolder : File, name : String, content : String) : Unit = {
    val outFile = new File(outFolder, name)
  }
}

class FileTraverser(start : File, converter : Converter) extends Traverser(converter) {
  def apply(outFolder : File) : Unit = {
    recurse(start) {s : String =>
      val n : Node = ??? // parse string to node
      val snippets = converter(n)
      snippets foreach { sn => 
        writeOut(outFolder, ???, sn.toString)
      }
    }
  }
  
  def recurse(f : File)(implicit proc : String => Unit) : Unit = {
    if (f.isDirectory()) {
      f.listFiles.foreach(recurse)
    } else { //file
      val s = new StringBuilder
      val in = new BufferedReader(new FileReader(f))
      var line: String = ""
      try {
        while (in.ready()) {
          line = in.readLine
          s.append(line + "\n")
        }
      } finally {
        in.close
      }
      proc(s.result)
    }
  }
}

class WebTraverser(uri : java.net.URL, converter : Converter) extends Traverser(converter) {
  def apply(outFolder : File) : Unit = ???
}