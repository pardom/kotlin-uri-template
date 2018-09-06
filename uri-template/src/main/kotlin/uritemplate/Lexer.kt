package uritemplate

internal object Lexer {

    fun lex(uriTemplate: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var pos = 0
        var char = uriTemplate[pos]

        // Consumption

        fun hasNext(): Boolean {
            return pos < uriTemplate.length
        }

        fun consume(): Boolean {
            pos++
            return if (hasNext()) {
                char = uriTemplate[pos]
                true
            } else {
                false
            }
        }

        fun check(vararg cs: Char): Boolean {
            for (c in cs) {
                if (char == c) {
                    return true
                }
            }
            return false
        }

        fun accept(c: Char): Boolean {
            if (check(c)) {
                consume()
                return true
            }
            return false
        }

        fun expect(c: Char): Boolean {
            if (accept(c)) return true
            throw IllegalStateException("Expected '$c' at position $pos, but found '$char'\n\t$uriTemplate")
        }

        fun peek(len: Int): CharArray {
            return uriTemplate.substring(pos).take(len).toCharArray()
        }

        fun seekWith(vararg cs: Char, block: () -> Unit) {
            while (hasNext() && !check(*cs)) {
                block()
            }
        }

        fun seek(vararg cs: Char) {
            seekWith(*cs) { consume() }
        }

        fun add(type: Token.Type, value: String) {
            tokens.add(Token(type, value))
        }

        fun add(type: Token.Type, value: Char) {
            add(type, value.toString())
        }

        // Grammar

        fun modifier() {
            if (accept(Token.MOD_EXPLODE)) {
                add(Token.Type.MODIFIER, Token.MOD_EXPLODE)
                seek(Token.SEPARATOR, Token.EXPR_END)
                return
            }
            if (accept(Token.MOD_PREFIX)) {
                val start = pos
                seek(Token.SEPARATOR, Token.EXPR_END)
                add(Token.Type.MODIFIER, uriTemplate.substring(start, pos))
                return
            }
        }

        fun varName() {
            val start = pos
            if (!isVarnameChars(*peek(3))) {
                throw SyntaxError(pos, "'$char' is not a valid varname character\n\t$uriTemplate")
            }
            seekWith(Token.MOD_PREFIX, Token.MOD_EXPLODE, Token.SEPARATOR, Token.EXPR_END) {
                if (!isVarnameChars(*peek(3))) {
                    throw SyntaxError(pos, "'$char' is not a valid varname character\n\t$uriTemplate")
                }
                consume()
            }
            add(Token.Type.VARNAME, uriTemplate.substring(start, pos))
        }

        fun varSpec() {
            seekWith(Token.SEPARATOR, Token.EXPR_END) {
                varName()
                if (check(Token.MOD_PREFIX, Token.MOD_EXPLODE)) {
                    modifier()
                }
                if (accept(Token.SEPARATOR)) {
                    add(Token.Type.SEPARATOR, Token.SEPARATOR)
                }
            }
        }

        fun varList() {
            seekWith(Token.SEPARATOR, Token.EXPR_END) {
                varSpec()
                if (accept(Token.SEPARATOR)) {
                    add(Token.Type.SEPARATOR, Token.SEPARATOR)
                }
            }
        }

        fun operator() {
            if (accept(Token.OP_RESERVED)) {
                add(Token.Type.OPERATOR, Token.OP_RESERVED)
                return
            }
            if (accept(Token.OP_FRAGMENT)) {
                add(Token.Type.OPERATOR, Token.OP_FRAGMENT)
                return
            }
            if (accept(Token.OP_LABEL)) {
                add(Token.Type.OPERATOR, Token.OP_LABEL)
                return
            }
            if (accept(Token.OP_PATH_SEGMENT)) {
                add(Token.Type.OPERATOR, Token.OP_PATH_SEGMENT)
                return
            }
            if (accept(Token.OP_PATH_PARAMETER)) {
                add(Token.Type.OPERATOR, Token.OP_PATH_PARAMETER)
                return
            }
            if (accept(Token.OP_QUERY)) {
                add(Token.Type.OPERATOR, Token.OP_QUERY)
                return
            }
            if (accept(Token.OP_CONTINUATION)) {
                add(Token.Type.OPERATOR, Token.OP_CONTINUATION)
                return
            }
        }

        fun expression() {
            operator()
            varList()
        }

        fun literal() {
            val start = pos
            seekWith(Token.EXPR_START) {
                if (!isLiteralChar(char)) {
                    throw SyntaxError(pos, "$char is not a valid literal character")
                }
                consume()
            }
            add(Token.Type.LITERAL, uriTemplate.substring(start, pos))
        }

        fun template() {
            while (hasNext()) {
                if (accept(Token.EXPR_START)) {
                    add(Token.Type.EXPR_START, Token.EXPR_START)
                    expression()
                    expect(Token.EXPR_END)
                    add(Token.Type.EXPR_END, Token.EXPR_END)
                } else {
                    literal()
                }
            }
        }

        // Lex

        template()

        return tokens
    }

    internal fun isUnreserved(c: Char): Boolean {
        return (c in 'a'..'z')
                || (c in 'A'..'Z')
                || (c in '0'..'9')
                || c == '-'
                || c == '.'
                || c == '_'
                || c == '~'
    }

    internal fun isReserved(c: Char): Boolean {
        return c == ':' || c == '/' || c == '?' || c == '#' || c == '[' || c == ']' || c == '@'
                || c == '!' || c == '$' || c == '&' || c == '\'' || c == '(' || c == ')'
                || c == '*' || c == '+' || c == ',' || c == ';' || c == '='
    }

    private fun isHexDig(c: Char): Boolean {
        return (c in 'a'..'f')
                || (c in 'A'..'F')
                || (c in '0'..'9')
    }

    private fun isPct(vararg cs: Char): Boolean {
        return cs.size == 3
                && cs[0] == '%'
                && isHexDig(cs[1])
                && isHexDig(cs[2])
    }

    private fun isVarnameChars(vararg cs: Char): Boolean {
        if (cs.isEmpty()) return false
        val c = cs[0]
        return (c in 'a'..'z')
                || (c in 'A'..'Z')
                || (c in '0'..'9')
                || c == '_'
                || c == '.'
                || isPct(*cs)
    }

    private fun isLiteralChar(c: Char): Boolean {
        return c != ' '
                && c != '"'
                && c != '\''
                && c != '%'
                && c != '<'
                && c != '>'
                && c != '\\'
                && c != '`'
                && c != '{'
                && c != '|'
                && c != '}'
    }

    internal data class Token(
        val type: Type,
        val value: String
    ) {

        enum class Type {
            LITERAL,
            EXPR_START,
            EXPR_END,
            OPERATOR,
            VARNAME,
            MODIFIER,
            SEPARATOR
        }

        companion object {

            internal const val EXPR_START = '{'
            internal const val EXPR_END = '}'

            internal const val OP_RESERVED = '+'
            internal const val OP_FRAGMENT = '#'
            internal const val OP_LABEL = '.'
            internal const val OP_PATH_SEGMENT = '/'
            internal const val OP_PATH_PARAMETER = ';'
            internal const val OP_QUERY = '?'
            internal const val OP_CONTINUATION = '&'

            internal const val MOD_PREFIX = ':'
            internal const val MOD_EXPLODE = '*'

            internal const val SEPARATOR = ','

        }

    }

}
