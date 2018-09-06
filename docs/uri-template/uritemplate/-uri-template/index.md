[uri-template](../../index.md) / [uritemplate](../index.md) / [UriTemplate](./index.md)

# UriTemplate

`class UriTemplate` [(source)](https://github.com/pardom/uri-template/tree/master/uri-template/src/main/kotlin/uritemplate/UriTemplate.kt#L14)

Represents a URI Template.

An instance of this class represents a URI template as defined by
[RFC 6570 - URI Template](https://tools.ietf.org/html/rfc6570).

### Parameters

`uriTemplate` - the URI template string

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `UriTemplate(uriTemplate: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)`<br>Constructs a new UriTemplate instance with the provided URI template string. |

### Functions

| Name | Summary |
|---|---|
| [expand](expand.md) | `fun expand(variables: `[`Map`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)`<*, *>): `[`URI`](http://docs.oracle.com/javase/6/docs/api/java/net/URI.html)<br>Expands a URI template given the input variables |
