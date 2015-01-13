package juicy.source

import scala.io.Source
import juicy.source.parser._
import juicy.source.tokenizer._

object CompilerMain {
  def main(args: Array[String]): Unit = {
    args.foreach(fname => {
        val file = Source.fromFile(fname).mkString
        val tokens = new TokenStream(file)
        val parser = new Parser(tokens)
    })
  }
}
