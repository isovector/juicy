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

  def build(asts: Seq[FileNode]): Seq[FileNode] = {
    handleErrors {
      val pkgtree = Resolver(asts)
      HardlyKnower(pkgtree)
      asts.foreach(Hashtag360NoScoper(_))
      val newasts =
        Disambiguator(asts, pkgtree)
        .map(Checker(_, pkgtree))

      newasts
        .foreach(AnalysisProbe(_))

      return newasts
    }

    asts
  }

  def main(args: Array[String]): Unit = {
    // HACK HACK HACK HACK HACK
    //val files = build(parseFiles(args.toList))
    val files = build(parseFiles("stdlib/io/OutputStream.java stdlib/io/PrintStream.java stdlib/io/Serializable.java stdlib/lang/Boolean.java stdlib/lang/Byte.java stdlib/lang/Character.java stdlib/lang/Class.java stdlib/lang/Cloneable.java stdlib/lang/Integer.java stdlib/lang/Number.java stdlib/lang/Object.java stdlib/lang/Short.java stdlib/lang/String.java stdlib/lang/System.java stdlib/util/Arrays.java joosc-test/Codegen.java".split(" ").toList))

    import java.io._
    files
      .filter(_.classes.length > 0)
      .sortBy(_.classes(0).classId)
      .foreach { f =>
        val fname = s"asm/${f.classes(0).name}.s"
        Target.withFile(fname) {
          f.classes.foreach { c =>
            Generator.emit(c)

            Some(new PrintWriter(fname)).foreach{p => p.write(Target.file.emitted); p.close}
          }
        }
      }

    Some(new PrintWriter("asm/global.s")).foreach{p => p.write(Target.global.emitted); p.close}
    Some(new PrintWriter("asm/types.cc")).foreach{p => p.write(Target.debug.toString); p.close}

    CompilerTerminate(0)
  }
}

