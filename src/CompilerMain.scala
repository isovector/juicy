package juicy.source

import juicy.source.ambiguous.Sexuality
import juicy.source.analysis.AnalysisProbe
import juicy.source.ast.FileNode
import juicy.source.checker.Checker
import juicy.source.parser._
import juicy.source.resolver._
import juicy.source.scoper._
import juicy.source.tokenizer._
import juicy.source.weeder._
import juicy.utils.CompilerError
import juicy.utils.visitor.VisitError
import scala.io.Source

case class CompilerExit(code: Int) extends Exception

object CompilerTerminate {
  var debug = false
  def apply(code: Int) {
    if (debug) {
      throw new CompilerExit(code)
    } else {
      System.exit(code)
    }
  }
}

object CompilerMain {
  def handleErrors(cmd: => Unit) = {
    try {
      cmd
    } catch {
      case e: CompilerError =>
        if (!CompilerTerminate.debug)
          System.err.println(e)
        CompilerTerminate(42)

      case e: VisitError =>
        if (!CompilerTerminate.debug)
          e.errors.map { error =>
            System.err.println(error)
          }
        CompilerTerminate(42)
    }
  }

  def parseFiles(files: Seq[String]): Seq[FileNode] = {
    files.map { fname =>
      val file = Source.fromFile(fname, "ISO-8859-1").mkString
      val tokens = new TokenStream(file, fname)

      var fast: Option[FileNode] = None
      handleErrors {
        val ast = new Parser(tokens).parseFile()
        Weeder(ast)
        fast = Some(ast)
      }

      fast
    }.toList.flatten
  }

  def build(asts: Seq[FileNode]) = {
    handleErrors {
      val pkgtree = Resolver(asts)
      HardlyKnower(pkgtree)
      asts.foreach(Hashtag360NoScoper(_))
      Sexuality(asts, pkgtree)
        .map(Checker(_, pkgtree))
        .foreach(AnalysisProbe(_))

    }

    CompilerTerminate(0)
  }

  def main(args: Array[String]): Unit = {
    build(parseFiles(args.toList))
    CompilerTerminate(0)
  }
}
