package juicy.source

import juicy.ast._
import juicy.ast.AST._
import juicy.source.tokenizer._
import juicy.source.tokenizer.Token._


class Parser(tokens: TokenStream) {
  def cur: Token = tokens.cur

  def ensure(source: String) = {
    val check = new Tokenizer(source).next()
    if (cur == check) {
      tokens.next()
    } else {
        // TODO: do something smarter here
      throw new Exception()
    }
  }

  def unwrap(token: Token): String = {
    token match {
      case Keyword(value) => value
      case Identifier(value) => value
      case Operator(value) => value
      case Modifier(value) => value
      case _ => throw new Exception("FOOL OF A TOOK! DON'T UNWRAP THAT!")
    }

  }

  def parseModifiers(): Modifiers.Value = {
    val mods = tokens.takeWhile{
        case Modifier(name) => true
        case _              => false
    }

    (0 /: mods)((agg, token) => agg | Modifiers.parse(unwrap(token)) )
  }

  def parseFile(): Node = {
    // TODO: packages
    // TODO: imports

    val mods = parseModifiers()
    parseClass(mods)
  }

  def parseClass(mods: Modifiers.Value): ClassDefn = {
    ensure("class")
    val name = unwrap(cur)
    new ClassDefn(name, mods, Seq(), Seq(), Seq(), Seq())
  }
}

