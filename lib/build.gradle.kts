import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin library project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.5/userguide/building_java_projects.html
 */

/**
 * Useful links:
 * - [jacoco gradle](https://docs.gradle.org/current/userguide/jacoco_plugin.html)
 * - [codecov update](https://app.codecov.io/gh/MrSchyzo/opiniom/new)
 * - [kover](https://github.com/Kotlin/kotlinx-kover)
 * - [detekt](https://detekt.dev/docs/intro)
 *  - [detekt suppress warnings](https://detekt.dev/docs/introduction/suppressing-rules/)
 * - [ktlint](https://ktlint.github.io/#getting-started)
 * - [publish to OSSRH](https://central.sonatype.org/publish/publish-guide/#deployment)
 *  - [gradle specific](https://central.sonatype.org/publish/publish-gradle/)
 *  - take inspiration from [here](https://github.com/doyaaaaaken/kotlin-csv/blob/master/build.gradle.kts)
 */

val whoami = "mrschyzo"
val projectName = rootProject.name

group = "io.github.$whoami"
version = "0.1.0"

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    id("org.jetbrains.dokka") version "1.7.10"

    // Code Coverage reports
    id("org.jetbrains.kotlinx.kover") version "0.5.0"
    // Linting
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    // Code smells, similar to Sonarqube
    id("io.gitlab.arturbosch.detekt") version "1.21.0"

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    `maven-publish`
    signing
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation("com.google.guava:guava:31.0.1-jre")

    testImplementation(kotlin("test"))

    testImplementation("io.mockk:mockk:1.12.4")

    testImplementation("io.strikt:strikt-core:0.34.1")
}

publishing {
    publications.all {
        (this as MavenPublication).pom {
            val projectPath = "$whoami/$projectName"

            name.set(projectName)
            description.set("Opinionated idiom")
            url.set("https://github.com/$projectPath")

            organization {
                name.set(group.toString())
                url.set("https://github.com/$whoami")
            }
            licenses {
                license {
                    name.set("MIT")
                    url.set("https://github.com/$projectPath/blob/master/LICENSE")
                }
            }
            scm {
                url.set("https://github.com/$projectPath")
                connection.set("scm:git:git://github.com/$projectPath.git")
                developerConnection.set(url.get())
            }
            developers {
                developer {
                    id.set(whoami)
                    name.set("Marco Catapano")
                }
            }
        }
    }
    repositories {
        maven {
            credentials {
                val nexusUsername: String? by project
                val nexusPassword: String? by project
                username = nexusUsername
                password = nexusPassword
            }

            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

tasks.jar {
    archiveBaseName.set(projectName)
}

signing {
    sign(publishing.publications)
}

tasks.test {
    useJUnitPlatform()
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.7"
}

tasks.koverVerify {
    rule {
        name = "Minimal line coverage rate in percent"
        bound {
            minValue = 85
        }
    }
}

tasks.koverXmlReport {
    isEnabled = true
}

kover {
    isDisabled = false
    coverageEngine.set(kotlinx.kover.api.CoverageEngine.INTELLIJ)
    intellijEngineVersion.set("1.0.656")
    jacocoEngineVersion.set("0.8.8")
    generateReportOnCheck = true
    disabledProjects = setOf()
    instrumentAndroidPackage = false
    runAllTestsForProjectTask = true
}
