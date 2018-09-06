package uritemplate

internal object Parser {

    private const val EMPTY_CHAR = 0.toChar()

    fun parse(tokens: List<Lexer.Token>): Node {
        var head: Node? = null
        var prev: Node? = null

        var pos = 0
        var token = tokens[pos]

        // Consumption

        fun hasNext(): Boolean {
            return pos < tokens.size
        }

        fun consume(): Boolean {
            pos++
            return if (hasNext()) {
                token = tokens[pos]
                true
            } else {
                false
            }
        }

        fun check(vararg ts: Lexer.Token.Type): Boolean {
            for (t in ts) {
                if (token.type == t) {
                    return true
                }
            }
            return false
        }

        fun accept(t: Lexer.Token.Type): Boolean {
            if (check(t)) {
                consume()
                return true
            }
            return false
        }

        fun expect(t: Lexer.Token.Type): Boolean {
            if (accept(t)) return true
            throw IllegalStateException("Expected $t at position $pos, but found ${token.type}")
        }

        fun seekWith(vararg ts: Lexer.Token.Type, block: () -> Unit) {
            while (hasNext() && !check(*ts)) block()
        }

        fun seek(vararg ts: Lexer.Token.Type) {
            seekWith(*ts) { consume() }
        }

        fun add(next: Node) {
            if (head == null) head = next
            if (prev != null) {
                next.prev = prev
                prev!!.next = next
            }
            prev = next
        }

        // Grammar

        fun modifier(): Node.Expression.VarSpec.Modifier {
            val value = token.value
            if (accept(Lexer.Token.Type.MODIFIER)) {
                if (value.startsWith(Lexer.Token.MOD_EXPLODE)) {
                    return Node.Expression.VarSpec.Modifier.Explode
                } else {
                    val maxLength = value.toInt()
                    return Node.Expression.VarSpec.Modifier.Prefix(maxLength)
                }
            }
            return Node.Expression.VarSpec.Modifier.None
        }

        fun varName(): String {
            val value = token.value
            expect(Lexer.Token.Type.VARNAME)
            return value
        }

        fun varSpec(): Node.Expression.VarSpec {
            return Node.Expression.VarSpec(varName(), modifier())
        }

        fun varList(): List<Node.Expression.VarSpec> {
            val varSpecs = mutableListOf<Node.Expression.VarSpec>()
            seekWith(Lexer.Token.Type.SEPARATOR, Lexer.Token.Type.EXPR_END) {
                varSpecs.add(varSpec())
                accept(Lexer.Token.Type.SEPARATOR)
            }
            return varSpecs
        }

        fun operator(): Node.Expression.Operator {
            val value = token.value
            if (accept(Lexer.Token.Type.OPERATOR)) {
                val opChar = value.firstOrNull()
                when (opChar) {
                    Lexer.Token.OP_RESERVED -> return Node.Expression.Operator.RESERVED
                    Lexer.Token.OP_FRAGMENT -> return Node.Expression.Operator.FRAGMENT
                    Lexer.Token.OP_LABEL -> return Node.Expression.Operator.LABEL
                    Lexer.Token.OP_PATH_SEGMENT -> return Node.Expression.Operator.PATH_SEGMENT
                    Lexer.Token.OP_PATH_PARAMETER -> return Node.Expression.Operator.PATH_PARAMETER
                    Lexer.Token.OP_QUERY -> return Node.Expression.Operator.QUERY
                    Lexer.Token.OP_CONTINUATION -> return Node.Expression.Operator.CONTINUATION
                }
            }
            return Node.Expression.Operator.NONE
        }

        fun expression() {
            add(Node.Expression(operator(), varList()))
        }

        fun literal() {
            val value = token.value
            expect(Lexer.Token.Type.LITERAL)
            add(Node.Literal(value))
        }

        fun template() {
            while (hasNext()) {
                if (accept(Lexer.Token.Type.EXPR_START)) {
                    expression()
                    expect(Lexer.Token.Type.EXPR_END)
                } else {
                    literal()
                }
            }
        }

        // Parse

        template()

        return head!!
    }

    sealed class Node {

        var prev: Node? = null
        var next: Node? = null

        abstract fun expand(args: Map<*, *>): String

        abstract fun match(uri: String): Map<String, String>

        abstract fun matches(uri: String): Boolean

        class Literal(
            private val value: String
        ) : Node() {

            override fun expand(args: Map<*, *>): String {
                if (next != null) {
                    return value + next!!.expand(args)
                }
                return value
            }

            override fun match(uri: String): Map<String, String> {
                if (next != null) {
                    return next!!.match(uri.substring(value.length))
                }
                return emptyMap()
            }

            override fun matches(uri: String): Boolean {
                if (next != null) {
                    return uri.startsWith(value, true) && next!!.matches(uri.substring(value.length))
                }
                return uri.startsWith(value, true)
            }

        }

        class Expression(
            private val operator: Operator,
            private val varSpecs: List<VarSpec>
        ) : Node() {

            override fun expand(args: Map<*, *>): String {
                val sb = StringBuilder()
                for ((i, varSpec) in varSpecs.withIndex()) {
                    val expanded = varSpec.expand(operator, args)
                    if (expanded.isNotEmpty()) {
                        if (i > 0) sb.append(operator.separator)
                        sb.append(expanded)
                    }
                }
                if (operator.prefix != EMPTY_CHAR && sb.isNotEmpty()) {
                    sb.insert(0, operator.prefix)
                }
                if (next != null) {
                    sb.append(next!!.expand(args))
                }
                return sb.toString()
            }

            override fun match(uri: String): Map<String, String> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun matches(uri: String): Boolean {
                return true
            }

            enum class Operator(
                val prefix: Char,
                val separator: Char,
                val encodeReserved: Boolean,
                val expandNamed: Boolean,
                val expandEmpty: Boolean
            ) {
                NONE(EMPTY_CHAR, ',', true, false, true),
                RESERVED(EMPTY_CHAR, ',', false, false, true),
                FRAGMENT('#', ',', false, false, true),
                LABEL('.', '.', true, false, true),
                PATH_SEGMENT('/', '/', true, false, true),
                PATH_PARAMETER(';', ';', true, true, false),
                QUERY('?', '&', true, true, true),
                CONTINUATION('&', '&', true, true, true)
            }

            data class VarSpec(
                val varName: String,
                var modifier: Modifier
            ) {

                fun expand(operator: Operator, args: Map<*, *>): String {
                    return modifier.expand(varName, args[varName], operator)
                }

                sealed class Modifier {

                    abstract fun expand(key: String, value: Any?, operator: Operator): String

                    protected fun expandObject(key: String, obj: Any?, operator: Operator, maxLength: Int): String {
                        if (obj == null) return ""
                        val value = obj.toString().take(maxLength)
                        val expanded = encode(value, operator.encodeReserved)
                        if (operator.expandNamed) {
                            if (!operator.expandEmpty && expanded.isEmpty()) {
                                return key
                            }
                            return "$key=$expanded"
                        }
                        return expanded
                    }

                    protected fun expandList(
                        key: String,
                        list: List<*>,
                        operator: Operator,
                        explode: Boolean
                    ): String {
                        if (list.isEmpty()) return ""
                        val sb = StringBuilder()
                        val separator = if (explode) operator.separator else ','
                        if (operator.expandNamed) {
                            sb.append(pctEncodeKey(key))
                            sb.append('=')
                        }
                        for ((i, obj) in list.withIndex()) {
                            if (i > 0) {
                                sb.append(separator)
                                if (explode && operator.expandNamed) {
                                    sb.append(pctEncodeKey(key))
                                    sb.append('=')
                                }
                            }
                            sb.append(encode(obj.toString(), operator.encodeReserved))
                        }
                        return sb.toString()
                    }

                    protected fun expandMap(
                        key: String,
                        map: Map<*, *>,
                        operator: Operator,
                        explode: Boolean
                    ): String {
                        if (map.isEmpty()) return ""
                        val sb = StringBuilder()
                        val separator = if (explode) operator.separator else ','
                        val delimiter = if (explode) '=' else ','
                        if (operator.expandNamed && !explode) {
                            sb.append(pctEncodeKey(key))
                            sb.append('=')
                        }
                        for ((i, entry) in map.entries.withIndex()) {
                            if (i > 0) sb.append(separator)
                            sb.append(pctEncodeKey(entry.key.toString()))
                            sb.append(delimiter)
                            sb.append(encode(entry.value.toString(), operator.encodeReserved))
                        }
                        return sb.toString()
                    }

                    private fun encode(value: String, encodeReserved: Boolean): String {
                        val sb = StringBuilder()
                        for (i in 0 until value.length) {
                            val c = value[i]
                            if (Lexer.isUnreserved(c) || (!encodeReserved && Lexer.isReserved(c))) {
                                sb.append(c)
                            } else {
                                sb.append(pctEncode(c))
                            }
                        }
                        return sb.toString()
                    }

                    private fun pctEncodeKey(s: String): String {
                        return s.toCharArray().joinToString("") { c ->
                            if (c.toInt() < 128) c.toString() else pctEncode(c)
                        }
                    }

                    private fun pctEncode(c: Char): String {
                        val cInt = c.toInt()
                        val encoded = when {
                            cInt < 128 -> {
                                arrayOf(cInt)
                            }
                            cInt in 128..2048 -> {
                                arrayOf(
                                    (cInt shr 6) or 192,
                                    (cInt and 63) or 128
                                )
                            }
                            else -> {
                                arrayOf(
                                    (cInt shr 12) or 224,
                                    ((cInt shr 6) and 63) or 128,
                                    (cInt and 63) or 128
                                )
                            }
                        }
                        val sb = StringBuilder()
                        for (char in encoded) {
                            sb.append('%')
                            sb.append(Integer.toHexString(char).toUpperCase())
                        }
                        return sb.toString()
                    }

                    object None : Modifier() {

                        override fun expand(key: String, value: Any?, operator: Operator): String {
                            return when (value) {
                                is Array<*> -> expandList(key, value.toList(), operator, false)
                                is Iterable<*> -> expandList(key, value.toList(), operator, false)
                                is Map<*, *> -> expandMap(key, value, operator, false)
                                else -> expandObject(key, value, operator, Integer.MAX_VALUE)
                            }
                        }

                    }

                    data class Prefix(
                        val maxLength: Int
                    ) : Modifier() {

                        override fun expand(key: String, value: Any?, operator: Operator): String {
                            return when (value) {
                                is Array<*> -> throw IllegalArgumentException("Prefix not applicable to list types.")
                                is Iterable<*> -> throw IllegalArgumentException("Prefix not applicable to list types.")
                                is Map<*, *> -> throw IllegalArgumentException("Prefix not applicable to map types.")
                                else -> expandObject(key, value, operator, maxLength)
                            }
                        }

                    }

                    object Explode : Modifier() {

                        override fun expand(key: String, value: Any?, operator: Operator): String {
                            return when (value) {
                                is Array<*> -> expandList(key, value.toList(), operator, true)
                                is Iterable<*> -> expandList(key, value.toList(), operator, true)
                                is Map<*, *> -> expandMap(key, value, operator, true)
                                else -> expandObject(key, value, operator, Integer.MAX_VALUE)
                            }
                        }

                    }

                }

            }

        }

    }

}
