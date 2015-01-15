package juicy.source

import juicy.source.parser._
import juicy.source.tokenizer._
import scala.io.Source

object CompilerMain {
  def main(args: Array[String]): Unit = {
    args.map { fname =>
      val file = Source.fromFile(fname).mkString
      val tokens = new TokenStream(file, fname)

      try {
        val ast = new Parser(tokens).parseFile()
      } catch {
        case UnexpectedException(msg) =>
          System.err.println(msg)
          System.exit(42)
      }
    }

    System.exit(0)
  }
}
