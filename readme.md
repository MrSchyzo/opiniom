# Opiniom - Opinionated Idiom

[![codecov](https://codecov.io/gh/MrSchyzo/opiniom/branch/master/graph/badge.svg?token=K3N3LSKXEY)](https://codecov.io/gh/MrSchyzo/opiniom)

Just a collection of data structures and extension functions for opinionated idioms.

## Usage

For gradle:
```kotlin
dependencies {
    // ... your stuff
    implementation("io.github.mrschyzo:opiniom:0.1.0")
    // .. your stuff
}
```

## Publish library

Prepare your `~/.gradle/gradle.properties` [accordingly](https://central.sonatype.org/publish/publish-gradle/#locate-and-examine-your-staging-repository)
and then run:

```shell
./gradlew publish
```

## git hooks to install
In your shell, run:
```shell
git config core.hooksPath ./git_config/hooks
```
