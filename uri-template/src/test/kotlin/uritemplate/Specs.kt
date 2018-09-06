package uritemplate

import uritemplate.spec.SpecGroup
import uritemplate.spec.SpecSuite
import uritemplate.spec.TestCase.Expansion
import uritemplate.spec.TestCase.Failure

object Specs {

    /**
     * Taken from [https://tools.ietf.org/html/rfc6570#section-1.2](https://tools.ietf.org/html/rfc6570#section-1.2)
     */
    private val BASIC_EXPANSION = SpecSuite(
        "Basic expansion",
        listOf(
            SpecGroup(
                "Level 1",
                mapOf(
                    "var" to "value",
                    "hello" to "Hello World!"
                ),
                listOf(
                    Expansion.Variable("{var}", "value"),
                    Expansion.Variable("{hello}", "Hello%20World%21")
                )
            ),
            SpecGroup(
                "Level 2",
                mapOf(
                    "var" to "value",
                    "hello" to "Hello World!",
                    "path" to "/foo/bar"
                ),
                listOf(
                    Expansion.Variable("{+var}", "value"),
                    Expansion.Variable("{+hello}", "Hello%20World!"),
                    Expansion.Variable("{+path}/here", "/foo/bar/here"),
                    Expansion.Variable("here?ref={+path}", "here?ref=/foo/bar"),
                    Expansion.Variable("X{#var}", "X#value"),
                    Expansion.Variable("X{#hello}", "X#Hello%20World!")
                )
            ),
            SpecGroup(
                "Level 3",
                mapOf(
                    "var" to "value",
                    "hello" to "Hello World!",
                    "empty" to "",
                    "path" to "/foo/bar",
                    "x" to "1024",
                    "y" to "768"
                ),
                listOf(
                    Expansion.Variable("map?{x,y}", "map?1024,768"),
                    Expansion.Variable("{x,hello,y}", "1024,Hello%20World%21,768"),
                    Expansion.Variable("{+x,hello,y}", "1024,Hello%20World!,768"),
                    Expansion.Variable("{+path,x}/here", "/foo/bar,1024/here"),
                    Expansion.Variable("{#x,hello,y}", "#1024,Hello%20World!,768"),
                    Expansion.Variable("{#path,x}/here", "#/foo/bar,1024/here"),
                    Expansion.Variable("X{.var}", "X.value"),
                    Expansion.Variable("X{.x,y}", "X.1024.768"),
                    Expansion.Variable("{/var}", "/value"),
                    Expansion.Variable("{/var,x}/here", "/value/1024/here"),
                    Expansion.Variable("{;x,y}", ";x=1024;y=768"),
                    Expansion.Variable("{;x,y,empty}", ";x=1024;y=768;empty"),
                    Expansion.Variable("{?x,y}", "?x=1024&y=768"),
                    Expansion.Variable("{?x,y,empty}", "?x=1024&y=768&empty="),
                    Expansion.Variable("?fixed=yes{&x}", "?fixed=yes&x=1024"),
                    Expansion.Variable("{&x,y,empty}", "&x=1024&y=768&empty=")
                )
            ),
            SpecGroup(
                "Level 4",
                mapOf(
                    "var" to "value",
                    "hello" to "Hello World!",
                    "path" to "/foo/bar",
                    "list" to listOf("red", "green", "blue"),
                    "keys" to mapOf("semi" to ";", "dot" to ".", "comma" to ",")
                ),
                listOf(
                    Expansion.Variable("{var:3}", "val"),
                    Expansion.Variable("{var:30}", "value"),
                    Expansion.Variable("{list}", "red,green,blue"),
                    Expansion.Variable("{list*}", "red,green,blue"),
                    Expansion.VariableList(
                        "{keys}", listOf(
                            "comma,%2C,dot,.,semi,%3B",
                            "comma,%2C,semi,%3B,dot,.",
                            "dot,.,comma,%2C,semi,%3B",
                            "dot,.,semi,%3B,comma,%2C",
                            "semi,%3B,comma,%2C,dot,.",
                            "semi,%3B,dot,.,comma,%2C"
                        )
                    ),
                    Expansion.VariableList(
                        "{keys*}", listOf(
                            "comma=%2C,dot=.,semi=%3B",
                            "comma=%2C,semi=%3B,dot=.",
                            "dot=.,comma=%2C,semi=%3B",
                            "dot=.,semi=%3B,comma=%2C",
                            "semi=%3B,comma=%2C,dot=.",
                            "semi=%3B,dot=.,comma=%2C"
                        )
                    ),
                    Expansion.Variable("{+path:6}/here", "/foo/b/here"),
                    Expansion.Variable("{+list}", "red,green,blue"),
                    Expansion.Variable("{+list*}", "red,green,blue"),
                    Expansion.VariableList(
                        "{+keys}", listOf(
                            "comma,,,dot,.,semi,;",
                            "comma,,,semi,;,dot,.",
                            "dot,.,comma,,,semi,;",
                            "dot,.,semi,;,comma,,",
                            "semi,;,comma,,,dot,.",
                            "semi,;,dot,.,comma,,"
                        )
                    ),
                    Expansion.VariableList(
                        "{+keys*}", listOf(
                            "comma=,,dot=.,semi=;",
                            "comma=,,semi=;,dot=.",
                            "dot=.,comma=,,semi=;",
                            "dot=.,semi=;,comma=,",
                            "semi=;,comma=,,dot=.",
                            "semi=;,dot=.,comma=,"
                        )
                    ),
                    Expansion.Variable("{#path:6}/here", "#/foo/b/here"),
                    Expansion.Variable("{#list}", "#red,green,blue"),
                    Expansion.Variable("{#list*}", "#red,green,blue"),
                    Expansion.VariableList(
                        "{#keys}", listOf(
                            "#comma,,,dot,.,semi,;",
                            "#comma,,,semi,;,dot,.",
                            "#dot,.,comma,,,semi,;",
                            "#dot,.,semi,;,comma,,",
                            "#semi,;,comma,,,dot,.",
                            "#semi,;,dot,.,comma,,"
                        )
                    ),
                    Expansion.VariableList(
                        "{#keys*}", listOf(
                            "#comma=,,dot=.,semi=;",
                            "#comma=,,semi=;,dot=.",
                            "#dot=.,comma=,,semi=;",
                            "#dot=.,semi=;,comma=,",
                            "#semi=;,comma=,,dot=.",
                            "#semi=;,dot=.,comma=,"
                        )
                    ),
                    Expansion.Variable("X{.var:3}", "X.val"),
                    Expansion.Variable("X{.list}", "X.red,green,blue"),
                    Expansion.Variable("X{.list*}", "X.red.green.blue"),
                    Expansion.VariableList(
                        "X{.keys}", listOf(
                            "X.comma,%2C,dot,.,semi,%3B",
                            "X.comma,%2C,semi,%3B,dot,.",
                            "X.dot,.,comma,%2C,semi,%3B",
                            "X.dot,.,semi,%3B,comma,%2C",
                            "X.semi,%3B,comma,%2C,dot,.",
                            "X.semi,%3B,dot,.,comma,%2C"
                        )
                    ),
                    Expansion.Variable("{/var:1,var}", "/v/value"),
                    Expansion.Variable("{/list}", "/red,green,blue"),
                    Expansion.Variable("{/list*}", "/red/green/blue"),
                    Expansion.Variable("{/list*,path:4}", "/red/green/blue/%2Ffoo"),
                    Expansion.VariableList(
                        "{/keys}", listOf(
                            "/comma,%2C,dot,.,semi,%3B",
                            "/comma,%2C,semi,%3B,dot,.",
                            "/dot,.,comma,%2C,semi,%3B",
                            "/dot,.,semi,%3B,comma,%2C",
                            "/semi,%3B,comma,%2C,dot,.",
                            "/semi,%3B,dot,.,comma,%2C"
                        )
                    ),
                    Expansion.VariableList(
                        "{/keys*}", listOf(
                            "/comma=%2C/dot=./semi=%3B",
                            "/comma=%2C/semi=%3B/dot=.",
                            "/dot=./comma=%2C/semi=%3B",
                            "/dot=./semi=%3B/comma=%2C",
                            "/semi=%3B/comma=%2C/dot=.",
                            "/semi=%3B/dot=./comma=%2C"
                        )
                    ),
                    Expansion.Variable("{;hello:5}", ";hello=Hello"),
                    Expansion.Variable("{;list}", ";list=red,green,blue"),
                    Expansion.Variable("{;list*}", ";list=red;list=green;list=blue"),
                    Expansion.VariableList(
                        "{;keys}", listOf(
                            ";keys=comma,%2C,dot,.,semi,%3B",
                            ";keys=comma,%2C,semi,%3B,dot,.",
                            ";keys=dot,.,comma,%2C,semi,%3B",
                            ";keys=dot,.,semi,%3B,comma,%2C",
                            ";keys=semi,%3B,comma,%2C,dot,.",
                            ";keys=semi,%3B,dot,.,comma,%2C"
                        )
                    ),
                    Expansion.VariableList(
                        "{;keys*}", listOf(
                            ";comma=%2C;dot=.;semi=%3B",
                            ";comma=%2C;semi=%3B;dot=.",
                            ";dot=.;comma=%2C;semi=%3B",
                            ";dot=.;semi=%3B;comma=%2C",
                            ";semi=%3B;comma=%2C;dot=.",
                            ";semi=%3B;dot=.;comma=%2C"
                        )
                    ),
                    Expansion.Variable("{?var:3}", "?var=val"),
                    Expansion.Variable("{?list}", "?list=red,green,blue"),
                    Expansion.Variable("{?list*}", "?list=red&list=green&list=blue"),
                    Expansion.VariableList(
                        "{?keys}", listOf(
                            "?keys=comma,%2C,dot,.,semi,%3B",
                            "?keys=comma,%2C,semi,%3B,dot,.",
                            "?keys=dot,.,comma,%2C,semi,%3B",
                            "?keys=dot,.,semi,%3B,comma,%2C",
                            "?keys=semi,%3B,comma,%2C,dot,.",
                            "?keys=semi,%3B,dot,.,comma,%2C"
                        )
                    ),
                    Expansion.VariableList(
                        "{?keys*}", listOf(
                            "?comma=%2C&dot=.&semi=%3B",
                            "?comma=%2C&semi=%3B&dot=.",
                            "?dot=.&comma=%2C&semi=%3B",
                            "?dot=.&semi=%3B&comma=%2C",
                            "?semi=%3B&comma=%2C&dot=.",
                            "?semi=%3B&dot=.&comma=%2C"
                        )
                    ),
                    Expansion.Variable("{&var:3}", "&var=val"),
                    Expansion.Variable("{&list}", "&list=red,green,blue"),
                    Expansion.Variable("{&list*}", "&list=red&list=green&list=blue"),
                    Expansion.VariableList(
                        "{&keys}", listOf(
                            "&keys=comma,%2C,dot,.,semi,%3B",
                            "&keys=comma,%2C,semi,%3B,dot,.",
                            "&keys=dot,.,comma,%2C,semi,%3B",
                            "&keys=dot,.,semi,%3B,comma,%2C",
                            "&keys=semi,%3B,comma,%2C,dot,.",
                            "&keys=semi,%3B,dot,.,comma,%2C"
                        )
                    ),
                    Expansion.VariableList(
                        "{&keys*}", listOf(
                            "&comma=%2C&dot=.&semi=%3B",
                            "&comma=%2C&semi=%3B&dot=.",
                            "&dot=.&comma=%2C&semi=%3B",
                            "&dot=.&semi=%3B&comma=%2C",
                            "&semi=%3B&comma=%2C&dot=.",
                            "&semi=%3B&dot=.&comma=%2C"
                        )
                    )
                )
            )
        )
    )

    /**
     * Taken from [https://github.com/uri-templates/uritemplate-test/blob/master/extended-tests.json](https://github.com/uri-templates/uritemplate-test/blob/master/extended-tests.json)
     */
    private val EXTENDED_EXPANSION = SpecSuite(
        "Extended expansion",
        listOf(
            SpecGroup(
                "Additional expansions 1",
                mapOf(
                    "id" to "person",
                    "token" to "12345",
                    "fields" to listOf(
                        "id",
                        "name",
                        "picture"
                    ),
                    "format" to "json",
                    "q" to "URI Templates",
                    "page" to "5",
                    "lang" to "en",
                    "geocode" to listOf(
                        "37.76",
                        "-122.427"
                    ),
                    "first_name" to "John",
                    "last.name" to "Doe",
                    "Some%20Thing" to "foo",
                    "number" to 6,
                    "long" to 37.76,
                    "lat" to -122.427,
                    "group_id" to "12345",
                    "query" to "PREFIX dc: <http://purl.org/dc/elements/1.1/> SELECT ?book ?who WHERE { ?book dc:creator ?who }",
                    "uri" to "http://example.org/?uri=http%3A%2F%2Fexample.org%2F",
                    "word" to "drücken",
                    "Stra%C3%9Fe" to "Grüner Weg",
                    "random" to "šöäŸœñê€£¥‡ÑÒÓÔÕÖ×ØÙÚàáâãäåæçÿ",
                    "assoc_special_chars" to mapOf(
                        "šöäŸœñê€£¥‡ÑÒÓÔÕ" to "Ö×ØÙÚàáâãäåæçÿ"
                    )
                ),
                listOf(
                    Expansion.Variable("{/id*}", "/person"),
                    Expansion.VariableList(
                        "{/id*}{?fields,first_name,last.name,token}", listOf(
                            "/person?fields=id,name,picture&first_name=John&last.name=Doe&token=12345",
                            "/person?fields=id,picture,name&first_name=John&last.name=Doe&token=12345",
                            "/person?fields=picture,name,id&first_name=John&last.name=Doe&token=12345",
                            "/person?fields=picture,id,name&first_name=John&last.name=Doe&token=12345",
                            "/person?fields=name,picture,id&first_name=John&last.name=Doe&token=12345",
                            "/person?fields=name,id,picture&first_name=John&last.name=Doe&token=12345"
                        )
                    ),
                    Expansion.VariableList(
                        "/search.{format}{?q,geocode,lang,locale,page,result_type}",
                        listOf(
                            "/search.json?q=URI%20Templates&geocode=37.76,-122.427&lang=en&page=5",
                            "/search.json?q=URI%20Templates&geocode=-122.427,37.76&lang=en&page=5"
                        )
                    ),
                    Expansion.Variable("/test{/Some%20Thing}", "/test/foo"),
                    Expansion.Variable("/set{?number}", "/set?number=6"),
                    Expansion.Variable("/loc{?long,lat}", "/loc?long=37.76&lat=-122.427"),
                    Expansion.Variable(
                        "/base{/group_id,first_name}/pages{/page,lang}{?format,q}",
                        "/base/12345/John/pages/5/en?format=json&q=URI%20Templates"
                    ),
                    Expansion.Variable(
                        "/sparql{?query}",
                        "/sparql?query=PREFIX%20dc%3A%20%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%20SELECT%20%3Fbook%20%3Fwho%20WHERE%20%7B%20%3Fbook%20dc%3Acreator%20%3Fwho%20%7D"
                    ),
                    Expansion.Variable(
                        "/go{?uri}",
                        "/go?uri=http%3A%2F%2Fexample.org%2F%3Furi%3Dhttp%253A%252F%252Fexample.org%252F"
                    ),
                    Expansion.Variable("/service{?word}", "/service?word=dr%C3%BCcken"),
                    Expansion.Variable("/lookup{?Stra%C3%9Fe}", "/lookup?Stra%C3%9Fe=Gr%C3%BCner%20Weg"),
                    Expansion.Variable(
                        "{random}",
                        "%C5%A1%C3%B6%C3%A4%C5%B8%C5%93%C3%B1%C3%AA%E2%82%AC%C2%A3%C2%A5%E2%80%A1%C3%91%C3%92%C3%93%C3%94%C3%95%C3%96%C3%97%C3%98%C3%99%C3%9A%C3%A0%C3%A1%C3%A2%C3%A3%C3%A4%C3%A5%C3%A6%C3%A7%C3%BF"
                    ),
                    Expansion.Variable(
                        "{?assoc_special_chars*}",
                        "?%C5%A1%C3%B6%C3%A4%C5%B8%C5%93%C3%B1%C3%AA%E2%82%AC%C2%A3%C2%A5%E2%80%A1%C3%91%C3%92%C3%93%C3%94%C3%95=%C3%96%C3%97%C3%98%C3%99%C3%9A%C3%A0%C3%A1%C3%A2%C3%A3%C3%A4%C3%A5%C3%A6%C3%A7%C3%BF"
                    )

                )
            ),
            SpecGroup(
                "Additional expansions 2",
                mapOf(
                    "id" to listOf("person", "albums"),
                    "token" to "12345",
                    "fields" to listOf("id", "name", "picture"),
                    "format" to "atom",
                    "q" to "URI Templates",
                    "page" to "10",
                    "start" to "5",
                    "lang" to "en",
                    "geocode" to listOf("37.76", "-122.427")
                ),
                listOf(
                    Expansion.VariableList(
                        "{/id*}", listOf(
                            "/person/albums",
                            "/albums/person"
                        )
                    ),
                    Expansion.VariableList(
                        "{/id*}{?fields,token}", listOf(
                            "/person/albums?fields=id,name,picture&token=12345",
                            "/person/albums?fields=id,picture,name&token=12345",
                            "/person/albums?fields=picture,name,id&token=12345",
                            "/person/albums?fields=picture,id,name&token=12345",
                            "/person/albums?fields=name,picture,id&token=12345",
                            "/person/albums?fields=name,id,picture&token=12345",
                            "/albums/person?fields=id,name,picture&token=12345",
                            "/albums/person?fields=id,picture,name&token=12345",
                            "/albums/person?fields=picture,name,id&token=12345",
                            "/albums/person?fields=picture,id,name&token=12345",
                            "/albums/person?fields=name,picture,id&token=12345",
                            "/albums/person?fields=name,id,picture&token=12345"
                        )
                    )
                )
            ),
            SpecGroup(
                "Additional expansions 3: empty variables",
                mapOf(
                    "empty_list" to emptyList<String>(),
                    "empty_assoc" to emptyMap<String, String>()
                ),
                listOf(
                    Expansion.VariableList("{/empty_list}", listOf("")),
                    Expansion.VariableList("{/empty_list*}", listOf("")),
                    Expansion.VariableList("{?empty_list}", listOf("")),
                    Expansion.VariableList("{?empty_list*}", listOf("")),
                    Expansion.VariableList("{?empty_assoc}", listOf("")),
                    Expansion.VariableList("{?empty_assoc*}", listOf(""))
                )
            ),
            SpecGroup(
                "Additional expansions 4: numeric keys",
                mapOf(
                    "42" to "The Answer to the Ultimate Question of Life, the Universe, and Everything",
                    "1337" to listOf("leet", "as", "it", "can", "be"),
                    "german" to mapOf(
                        "11" to "elf",
                        "12" to "zwölf"
                    )
                ),
                listOf(
                    Expansion.Variable(
                        "{42}",
                        "The%20Answer%20to%20the%20Ultimate%20Question%20of%20Life%2C%20the%20Universe%2C%20and%20Everything"
                    ),
                    Expansion.Variable(
                        "{?42}",
                        "?42=The%20Answer%20to%20the%20Ultimate%20Question%20of%20Life%2C%20the%20Universe%2C%20and%20Everything"
                    ),
                    Expansion.Variable("{1337}", "leet,as,it,can,be"),
                    Expansion.Variable("{?1337*}", "?1337=leet&1337=as&1337=it&1337=can&1337=be"),
                    Expansion.VariableList(
                        "{?german*}", listOf(
                            "?11=elf&12=zw%C3%B6lf", "?12=zw%C3%B6lf&11=elf"
                        )
                    )
                )
            ),
            SpecGroup(
                "Additional expansions 5: explode combinations",
                mapOf(
                    "id" to "admin",
                    "token" to "12345",
                    "tab" to "overview",
                    "keys" to mapOf(
                        "key1" to "val1",
                        "key2" to "val2"
                    )
                ),
                listOf(
                    Expansion.VariableList(
                        "{?id,token,keys*}", listOf(
                            "?id=admin&token=12345&key1=val1&key2=val2",
                            "?id=admin&token=12345&key2=val2&key1=val1"
                        )
                    ),
                    Expansion.VariableList(
                        "{/id}{?token,keys*}", listOf(
                            "/admin?token=12345&key1=val1&key2=val2",
                            "/admin?token=12345&key2=val2&key1=val1"
                        )
                    ),
                    Expansion.VariableList(
                        "{?id,token}{&keys*}", listOf(
                            "?id=admin&token=12345&key1=val1&key2=val2",
                            "?id=admin&token=12345&key2=val2&key1=val1"
                        )
                    ),
                    Expansion.VariableList(
                        "/user{/id}{?token,tab}{&keys*}", listOf(
                            "/user/admin?token=12345&tab=overview&key1=val1&key2=val2",
                            "/user/admin?token=12345&tab=overview&key2=val2&key1=val1"
                        )
                    )
                )
            )
        )
    )

    /**
     * Taken from [https://github.com/uri-templates/uritemplate-test/blob/master/negative-tests.json](https://github.com/uri-templates/uritemplate-test/blob/master/negative-tests.json)
     */
    private val FAILURES = SpecSuite(
        "Failures",
        listOf(
            SpecGroup(
                "Parsing",
                mapOf(
                    "id" to "thing",
                    "var" to "value",
                    "hello" to "Hello World!",
                    "with space" to "fail",
                    " leading_space" to "Hi!",
                    "trailing_space " to "Bye!",
                    "empty" to "",
                    "path" to "/foo/bar",
                    "x" to "1024",
                    "y" to "768",
                    "list" to listOf("red", "green", "blue"),
                    "keys" to mapOf(
                        "semi" to ";",
                        "dot" to ".",
                        "comma" to ","
                    ),
                    "example" to "red",
                    "searchTerms" to "uri templates",
                    "~thing" to "some-user",
                    "default-graph-uri" to listOf("http://www.example/book/", "http://www.example/papers/"),
                    "query" to "PREFIX dc: <http://purl.org/dc/elements/1.1/> SELECT ?book ?who WHERE { ?book dc:creator ?who }"
                ),
                listOf(
                    Failure("{/id*"),
                    Failure("/id*}"),
                    Failure("{/?id}"),
                    Failure("{var:prefix}"),
                    Failure("{hello:2*}"),
                    Failure("{??hello}"),
                    Failure("{!hello}"),
                    Failure("{with space}"),
                    Failure("{ leading_space}"),
                    Failure("{trailing_space }"),
                    Failure("{=path}"),
                    Failure("{\$var}"),
                    Failure("{|var*}"),
                    Failure("{*keys?}"),
                    Failure("{?empty=default,var}"),
                    Failure("{var}{-prefix|/-/|var}"),
                    Failure("?q={searchTerms}&amp;c={example:color?}"),
                    Failure("x{?empty|foo=none}"),
                    Failure("/h{#hello+}"),
                    Failure("/h#{hello+}"),
                    Failure("?{-join|&|var,list}"),
                    Failure("/people/{~thing}"),
                    Failure("/{default-graph-uri}"),
                    Failure("/sparql{?query,default-graph-uri}"),
                    Failure("/sparql{?query){&default-graph-uri*}"),
                    Failure("/resolution{?x, y}")
                )
            ),
            SpecGroup(
                "Expansion",
                mapOf(
                    "keys" to mapOf(
                        "semi" to ";",
                        "dot" to ".",
                        "comma" to ","
                    )
                ),
                listOf(
                    Failure("{keys:1}"),
                    Failure("{+keys:1}"),
                    Failure("{;keys:1*}")
                )
            )
        )
    )

    val ALL = listOf(
        BASIC_EXPANSION,
        EXTENDED_EXPANSION,
        FAILURES
    )

}
