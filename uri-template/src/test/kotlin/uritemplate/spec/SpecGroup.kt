package uritemplate.spec

data class SpecGroup(
    val description: String,
    val variables: Map<String, Any>,
    val testCases: Collection<TestCase>
)
