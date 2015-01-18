package juicy.source

import juicy.source.parser._
import juicy.source.tokenizer._
import juicy.source.weeder._
import juicy.utils.CompilerError
import juicy.utils.visitor.VisitError
import scala.io.Source

object CompilerMain {
  def main(args: Array[String]): Unit = {
    args.map { fname =>
      val file = Source.fromFile(fname).mkString
      val tokens = new TokenStream(file, fname)

      try {
        val ast = new Parser(tokens).parseFile()
        Weeder(ast)
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

    System.exit(0)
  }
}
