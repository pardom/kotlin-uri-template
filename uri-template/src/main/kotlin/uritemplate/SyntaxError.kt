package uritemplate

class SyntaxError(
    position: Int,
    message: String
) : RuntimeException("Syntax error at position $position: $message")
