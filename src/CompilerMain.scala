package juicy.source

import juicy.source.parser._
import juicy.source.resolver._
import juicy.source.tokenizer._
import juicy.source.weeder._
import juicy.utils.CompilerError
import juicy.utils.visitor.VisitError
import scala.io.Source

object CompilerMain {
  def handleErrors(cmd: => Unit) = {
    try {
      cmd
    } catch {
      case e: CompilerError =>
        System.err.println(e)
        System.exit(42)

      case e: VisitError =>
        e.errors.map { error =>
          System.err.println(error)
        }
        System.exit(42)
    }
  }

  def main(args: Array[String]): Unit = {
    val asts = args.map { fname =>
      val file = Source.fromFile(fname).mkString
      val tokens = new TokenStream(file, fname)

      var fast: Option[juicy.source.ast.FileNode] = None
      handleErrors {
        val ast = new Parser(tokens).parseFile()
        Weeder(ast)
        fast = Some(ast)
      }

      fast
    }.toList.flatten

    handleErrors {
      HardlyKnower(Resolver(asts))
    }

    System.exit(0)
  }
}
