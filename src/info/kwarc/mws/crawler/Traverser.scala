package info.kwarc.mws.crawler

import java.io._
import scala.xml._
import org.htmlcleaner._

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

case class GapSnippet(title : String, name : String, id : String, body : Node)

class GapXMLConverter extends BaseConverter("gapdoc", "temahtml") {
  
  val url = new java.net.URL("http://latexml.mathweb.org/convert");
  val latexmlService = new LaTeXMLService(url);
      
//println(latexmlService.convert("12", "$$E=mc^2$$"));

  def error(s : String) = println(s)
  def isSectionStart(n : Node) : Boolean = {
    n.label == "p" && n.child.size == 1 && n.child.head.label == "a" && !(n.child.head \ "@id").text.isEmpty 
  }
  
  //assumes isSectionStart holds
  def getSectionStartId(n : Node) : String = {
    (n.child.head \ "@id").text
  }
  
  def apply(n : Node) : List[Node] = {
    (n \\ "body").toList match {
      case body :: Nil => 
        val snippets = processBody(body)
        val docs = snippets map {gs => 
          processSnippet(gs.title, gs.name, gs.id, gs.body)
        }
        docs.toList
      case Nil => 
        error("no body found")
        Nil
      case _ => 
        error("more than one body found")
        Nil
    }
  }
  
  def processSnippet(title : String, name : String, id : String, body : Node) : Node = {
    val url = "http://www.gap-system.org/Manuals/doc/ref/" + name + "#" + id
    val procedSnippet = replaceTex(body)
    <html> 
      <head>
        <title>{title}</title>
        <meta name="url" content={url}></meta>
      </head>
      {procedSnippet}
    </html>
  }
  
  def replaceTex(n : Node) : Node = n match {
    case n if n.label == "span" && ((n \ "@class").text == "SimpleMath") => 
      val tex = n.child.mkString(" ")
      val mathS = latexmlService.convert("math", tex)
      val math = XML.loadString(mathS)
      val hash = (new java.util.Random).nextLong.toString
      val attr = new UnprefixedAttribute("id", hash, math.attributes)
      new Elem(math.prefix, math.label, attr, math.scope, math.child :_*)
    case e : Elem =>
      val nc = e.child.map(replaceTex)
      new Elem(e.prefix, e.label, e.attributes, e.scope, e.minimizeEmpty, nc :_*)
    case other => other
  }
  
  
  //returns a list of snippets (with id)
  def processBody(body : Node) : List[GapSnippet] = {
    var snippets : List[GapSnippet] = Nil
    var foundHead  = false
    var currHead : String = null
    var currTitle = "TODO"
    var currName = "TODO"
    var currSnippet : List[Node] = Nil
    var prevIsSectionStart = false
    body.child foreach {
      case c if isSectionStart(c) => 
        if (foundHead) {
          val gs = GapSnippet(currTitle, currName, currHead, <body> {currSnippet} </body>)
          snippets ::= gs
        } else {
          foundHead = true
        }
        prevIsSectionStart = true
        currHead = getSectionStartId(c)
        currSnippet = Nil
      case c if foundHead => 
        if (prevIsSectionStart && (c.label == "h5" || c.label == "h4") ) {
          currTitle = c.text
        }
        prevIsSectionStart = false
        currSnippet :+= c //TODO inefficient
      case c if !foundHead => //ignore for now
    }
    if (foundHead) {
      val gs = GapSnippet(currTitle, currName, currHead, <body> {currSnippet} </body>)
      snippets ::= gs
    }
    
    snippets
  }
  
}

abstract class Traverser(converter : Converter) {
  def apply(outFolder : File) : Unit
  def writeOut(outFolder : File, name : String, content : String) : Unit = {
    val outFile = new File(outFolder, name)
    val pr = new PrintWriter(outFile)
    pr.write(content)
    pr.close()
  }
}

class FileTraverser(start : File, converter : Converter) extends Traverser(converter) {
  var counter = 1
  
  
  def apply(outFolder : File) : Unit = {
    recurse(start) {s : String =>
      val n : Node = XML.loadString(s) // parse string to node
      val snippets = converter(n)
      println(snippets)
      snippets foreach { sn => 
        writeOut(outFolder, counter.toString + ".html", sn.toString)
        counter += 1
      }
    }
  }
  
  
  def parseXML(s : String) : String = {
    val props : CleanerProperties = new CleanerProperties();
 
    // set some properties to non-default values
    props.setTranslateSpecialEntities(true);
    props.setTransResCharsToNCR(true);
    props.setOmitComments(true);
 
    // do parsing
    val tagNode : TagNode  = new HtmlCleaner(props).clean(s);
 
    // serialize to xml file
    val sr = new SimpleXmlSerializer(props)
    sr.getAsString(tagNode)
    val newS = sr.getAsString(tagNode)
    /*   
    .writeToFile(
      tagNode, "chinadaily.xml", "utf-8"
    );
    */
    
    newS
  }
  
  def recurse(f : File)(implicit proc : String => Unit) : Unit = {
    if (f.isDirectory()) {
      f.listFiles.foreach(recurse)
    } else { //file
      try {
        val s = new StringBuilder
        val in = new BufferedReader(new FileReader(f))
        var line: String = ""
        try {
          while (in.ready()) {
            line = in.readLine
            if (!line.startsWith("<link") && !line.startsWith("<meta")) {
              line = line.replaceAll("<br>", "<br/>")
              s.append(line + "\n")
            }
          }
        } finally {
          in.close
        }
        val cleanedS = parseXML(s.result)
        proc(cleanedS)
      } catch {
        case e : Exception => 
          println(e.getMessage)
          println("Failed on: " + f.toString())
      }
    }
  }
}

class WebTraverser(uri : java.net.URL, converter : Converter) extends Traverser(converter) {
  def apply(outFolder : File) : Unit = ???
}