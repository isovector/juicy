package juicy.source.tokenizer

case class SourceLocation(file: String, line: Int, col: Int) {
    override def toString() = s"$file: line $line, column $col"
}

