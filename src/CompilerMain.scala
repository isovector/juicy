package juicy.source

import juicy.codegen._
import juicy.source.analysis.AnalysisProbe
import juicy.source.ast.FileNode
import juicy.source.checker.Checker
import juicy.source.disambiguator.Disambiguator
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

  def build(asts: Seq[FileNode]): (PackageTree, Seq[FileNode]) = {
    handleErrors {
      val pkgtree = Resolver(asts)
      HardlyKnower(pkgtree)
      asts.foreach(Hashtag360NoScoper(_))
      val newasts =
        Disambiguator(asts, pkgtree)
        .map(Checker(_, pkgtree))

      newasts
        .foreach(AnalysisProbe(_))

      return (pkgtree, newasts)
    }

    throw new Exception("you should never get here")
  }

  def main(args: Array[String]): Unit = {
    val run_debug = true

    val (pkgtree, files) =
      if (!run_debug)
        build(parseFiles(args.toList))
      else
        build(parseFiles(Seq(
          "stdlib/lang/Object.java",
          "stdlib/lang/Number.java",
          "stdlib/io/Serializable.java",
          "stdlib/io/OutputStream.java",
          "stdlib/io/PrintStream.java",
          "stdlib/lang/Boolean.java",
          "stdlib/lang/Byte.java",
          "stdlib/lang/Character.java",
          "stdlib/lang/Class.java",
          "stdlib/lang/Cloneable.java",
          "stdlib/lang/Integer.java",
          "stdlib/lang/Short.java",
          "stdlib/lang/String.java",
          "stdlib/lang/System.java",
          "stdlib/util/Arrays.java",
          "joosc-test/Codegen.java",
          "joosc-test/Codegen2.java",
          "joosc-test/FunInterface.java"
        )))

    if (files.length > 0)
      Driver(pkgtree, files)
    CompilerTerminate(0)
  }
}

