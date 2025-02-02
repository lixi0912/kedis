# Kedis

[![Maven Central](https://img.shields.io/maven-central/v/io.github.domgew/kedis)](https://central.sonatype.com/search?q=kedis&namespace=io.github.domgew)
[![Latest Tag](https://img.shields.io/github/v/tag/domgew/kedis?label=latest%20tag&cacheSeconds=180)](https://github.com/domgew/kedis/tags)
[![Publish](https://img.shields.io/github/actions/workflow/status/domgew/kedis/.github%2Fworkflows%2Fpublish.yml?label=publish&cacheSeconds=180)](https://github.com/domgew/kedis/actions/workflows/publish.yml)
[![Test](https://img.shields.io/github/actions/workflow/status/domgew/kedis/.github%2Fworkflows%2Ftest.yml?branch=development&label=test&cacheSeconds=180)](https://github.com/domgew/kedis/actions/workflows/test.yml)
![Kotlin](https://img.shields.io/github/languages/top/domgew/kedis?cacheSeconds=86400)
[![Licence: MIT](https://img.shields.io/github/license/domgew/kedis?cacheSeconds=86400)](./LICENSE)

Kedis is a Redis client library for Kotlin Multiplatform (JVM + Native). This is possible via [Ktor Network sockets](https://ktor.io/docs/servers-raw-sockets.html), which
provides native and JVM sockets with a unified interface.

* [Installation](#installation)
* [Documentation](#documentation)
* [Targets](#targets)
* [Library Comparison](#library-comparison)
* [Examples](#examples)

## Installation

```kotlin
dependencies {
    // ...

    implementation("io.github.domgew:kedis:<current_version>")

    // OR just for JVM:
    implementation("io.github.domgew:kedis-jvm:<current_version>")

    // ...
}
```

```kotlin
repositories {
    mavenCentral()

    // ...
}
```

## Documentation

See Dokka-generated [docs](https://javadoc.io/doc/io.github.domgew/kedis/latest/kedis/io.github.domgew.kedis/index.html). For available commands see the documentation of the KedisClient interface and the KedisConfiguration for the available configuration options.

## Targets

**Supported Targets**:

* JVM
* Native: Linux X64
* Native: Linux ARM64
* Native: macOS X64
* Native: macOS ARM64

**Potential Future Targets** (mostly currently no Ktor Network support):

* Native: MinGW X64
* JS: NodeJS
* JVM: GraalVM Native

**Non-Targets - Never Coming**:

* Native 32 bit targets
* Native consumer targets (android, iOS, tvOS, watchOS, ...)
* JS: Browser

## Library Comparison

|                               |               Kedis                | [Kreds](https://github.com/crackthecodeabhi/kreds) |
|:------------------------------|:----------------------------------:|:--------------------------------------------------:|
| Automated Integration Tests   |              &check;               |                      &check;                       |
| JVM Support                   |              &check;               |                      &check;                       |
| Native Linux X64 Support      |              &check;               |                      &cross;                       |
| Native Linux ARM64 Support    |              &check;               |                      &cross;                       |
| Native macOS X64 Support      |              &check;               |                      &cross;                       |
| Native macOS ARM64 Support    |              &check;               |                      &cross;                       |
| Host + Port Support           |              &check;               |                      &check;                       |
| UNIX Socket Support           |              &check;               |                      &cross;                       |
| Binary Data Support           |              &check;               |                      &cross;                       |
| Stable Authentication         |      &check; (with AutoAuth)       |         &cross; (errors when reconnecting)         |
| Mature                        |              &cross;               |                      &check;                       |
| Full-Featured                 |              &cross;               |                      &check;                       |
| Pub-Sub Support               |              &cross;               |                      &check;                       |
| Pipelining Support            |              &cross;               |                      &check;                       |
| GraalVM Native Support        |              &cross;               |                      &cross;                       |
| Exclusive Configuration       | Compile Time / Sealed Polymorthism |            Run Time / Builder Exception            |
| Responses                     |           Strictly Typed           |                      Semi-Raw                      |
| Networking                    |       Ktor Network (Kotlin)        |                    Netty (Java)                    |
| Redis Protocol (En-/Decoding) |          Custom (Kotlin)           |                    Netty (Java)                    |
| Redis Protocol (Interfacing)  |          Custom (Kotlin)           |                  Custom (Kotlin)                   |

## Examples

See [example project](./example).

Caching concept ("get or generate") with gradual service degradation:

* Try to connect to the Redis server (KedisClient.connect) - here you would already see, whether the server is
  reachable, if not, fall back to generation -> EXIT with generation
* Try to get the value for the key (KedisClient.get/getBinary) - if it is `null`, it does not exist - it could however
  fail under some circumstances, so use a try-catch block -> EXIT with generation
* When a non-null value was retrieved, you can return it (maybe close the connection/client (KedisClient.close) - with
  dependency injection request scopes, this might however not be desired) -> EXIT
* Otherwise, use a callback to generate the value (e.g. call an external API)
* Try to set the value for the key (KedisClient.set/setBinary) with the desired options (e.g. time-to-live, value
  replacement, ...) - this might however fail under some circumstances
* Return the generated value (maybe close the connection)

```mermaid
---
title: Get or Generate
---

flowchart
    start([Value requested])
    isAvailable{Is Redis available?}
    getFromCache[Get from cache]
    generateBeforeExit[Generate]
    generateBeforeWrite[Generate]
    writeToCache[Write to cache]
    hasCachedValue{Has cached value?}
    returnValue([Return value])
    exitWithError([Exit with error])

    start --> isAvailable
    isAvailable -- no --> generateBeforeExit
    isAvailable -- yes --> getFromCache
    generateBeforeExit -- success --> returnValue
    generateBeforeExit -- error --> exitWithError
    getFromCache -- error --> generateBeforeExit
    getFromCache -- success --> hasCachedValue
    hasCachedValue -- no --> generateBeforeWrite
    hasCachedValue -- yes --> returnValue
    generateBeforeWrite -- success --> writeToCache
    generateBeforeWrite -- error --> exitWithError
    writeToCache -- error --> returnValue
    writeToCache -- success --> returnValue
```
