package uritemplate

/**
 * Represents a URI Template.
 *
 * An instance of this class represents a URI template as defined by
 * [RFC 6570 - URI Template](https://tools.ietf.org/html/rfc6570).
 *
 * @param uriTemplate the URI template string
 * @constructor Constructs a new UriTemplate instance with the provided URI template string.
 */
class UriTemplate(uriTemplate: String) {

    private val head = Parser.parse(Lexer.lex(uriTemplate))

    /**
     * Expands a URI template given the input variables.
     *
     * @param variables map of values
     */
    fun expand(variables: Map<*, *>): String {
        return head.expand(variables)
    }

    fun match(uri: String): Map<String, String> {
        return head.match(uri)
    }

    fun matches(uri: String): Boolean {
        return head.matches(uri)
    }

}
