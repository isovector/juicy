package juicy.source.parser

import juicy.source.ast._
import juicy.source.tokenizer._
import juicy.source.tokenizer.Token._
import juicy.utils.CompilerError
import juicy.utils.visitor._

object ParserUtils {
  private val memoized = new collection.mutable.HashMap[String, Token]

  // Add asToken() to strings
  case class RichString(underlying: String) {
    def asToken(): Token = {
      memoized
        .get(underlying)
        .getOrElse {
          val token = new Tokenizer(underlying).next()
          memoized += underlying -> token
          token
        }
    }
  }

  implicit def strToToken(underlying: String) = new RichString(underlying)
}

case class UnexpectedError(msg: String, from: SourceLocation) extends CompilerError

// Helper functions for building relatively terse grammars
trait ParserUtils {
  def cur: Token
  def next(): Unit

  import ParserUtils._

  // Is the current token equivalent to the token described by source?
  def check(source: String) = cur == source.asToken

  // Is the current token an identifier?
  def checkIdentifier() =
    cur match {
      case Identifier(_) => true
      case _             => false
    }

  def checkModifier() =
    cur match {
      case Modifier(_) => true
      case _           => false
    }

  def checkPrimitive() =
    cur match {
      case Primitive(_) => true
      case _            => false
    }

  // Ensure the next token is equivalent to source and advance the stream
  // Returns the matching token
  def ensure(source: String): Token = {
    if (check(source)) {
      val result = cur
      next()
      cur
    } else {
      throw Expected(s"`$source`")
    }
  }

  // Ensure the next token is an identifier. Returns the matching token
  def ensureIdentifier(): Token = {
    if (!checkIdentifier()) {
      throw Expected("identifier")
    }

    val result = cur
    next()
    result
  }

  // Ensure the next token is an identifier. Returns the matching token
  def ensurePrimitive(): Token = {
    if (!checkPrimitive()) {
      throw Expected("primitive")
    }

    val result = cur
    next()
    result
  }

  // Get the string value wrapped by a simple token
  def unwrap(token: Token): String = {
    token match {
      case Keyword(value)    => value
      case Primitive(value)  => value
      case Identifier(value) => value
      case Operator(value)   => value
      case Modifier(value)   => value
      case _                 => throw Expected("keyword, identifier, operator or modifier")
    }
  }

  // Match tokens described by the aggregator function, and eat a delimiter
  // between each one. Matches 1 or more.
  // EXAMPLE: can be used to parse `a, b, c`
  def delimited[T](delimiter: Token)(aggregator: => T): Seq[T] = {
    val results = new scala.collection.mutable.MutableList[T]()
    results += aggregator
    while(cur == delimiter) {
      next()
      results += aggregator
    }
    results.toList
  }

  // Implement a kleene star (0 or more) of a pattern described by aggregator
  def kleene[T](until: Token)(aggregator: => T): Seq[T] = {
    val results = new scala.collection.mutable.MutableList[T]()
    while (cur != until) results += aggregator
    results.toList
  }

  // Macro to add the original source token to a parser pattern
  def withSource[T <: Visitable](parser: => T): T = {
    val source = cur
    val result = parser

    // Uncomment this for better debugging (but it's slow)
/*
    val default = result.originalToken
    result.visit((_: Unit, _: Unit) => {})
    { case (node, context) =>
      node match {
        case Before(n: Visitable) =>
          if (n.originalToken == default)
            n.originalToken = source

        case _ =>
      }
    }*/

    result.originalToken = source
    result
  }

  def Expected(what: String) =
    new UnexpectedError(
      "Expected " + what + ", but got `" + cur.toString + "` instead",
      cur.from)
}

