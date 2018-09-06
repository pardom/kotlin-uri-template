package uritemplate

import com.google.common.truth.Truth.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import uritemplate.spec.TestCase
import uritemplate.spec.TestCase.Expansion
import kotlin.test.assertFails

class UriTemplateTest : Spek({

    describe("a UriTemplate") {
        for (specSuite in Specs.ALL) {
            group(specSuite.description) {
                for (specGroup in specSuite.specGroups) {
                    val variables = specGroup.variables
                    group(specGroup.description) {
                        for (testCase in specGroup.testCases) {
                            given("a template of '${testCase.template}'") {
                                on("expansion of variables $variables") {
                                    when (testCase) {
                                        is Expansion.Variable -> {
                                            val expected = testCase.expected
                                            val actual = UriTemplate(testCase.template).expand(variables)
                                            it("should expand to '$expected'") {
                                                assertThat(actual).isEqualTo(expected)
                                            }
                                        }
                                        is Expansion.VariableList -> {
                                            val expected = testCase.expected
                                            val actual = UriTemplate(testCase.template).expand(variables)
                                            it("should expand to any of '$expected'") {
                                                assertThat(expected).contains(actual)
                                            }
                                        }
                                        is TestCase.Match -> {
                                            val expected = testCase.variables
                                            val actual = UriTemplate(testCase.template).match(testCase.uri)
                                            it("should match all of '$expected'") {
                                                assertThat(expected).containsExactlyEntriesIn(actual)
                                            }
                                        }
                                        is TestCase.Matches -> {
                                            val matches = UriTemplate(testCase.template).matches(testCase.uri)
                                            it("should match the template") {
                                                assertThat(matches).isTrue()
                                            }
                                        }
                                        is TestCase.Failure -> {
                                            it("should fail with an exception") {
                                                assertFails {
                                                    UriTemplate(testCase.template).expand(variables)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
})
