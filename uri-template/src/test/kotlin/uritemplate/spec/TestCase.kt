package uritemplate.spec

sealed class TestCase {

    abstract val template: String

    sealed class Expansion : TestCase() {

        data class Variable(
            override val template: String,
            val expected: String
        ) : Expansion()

        data class VariableList(
            override val template: String,
            val expected: List<String>
        ) : Expansion()

    }

    data class Match(
        override val template: String,
        val uri: String,
        val variables: Map<String, String>
    ) : TestCase()

    data class Matches(
        override val template: String,
        val uri: String
    ) : TestCase()

    data class Failure(
        override val template: String
    ) : TestCase()

}
