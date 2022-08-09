import org.gradle.jvm.tasks.Jar
import java.net.URL


plugins {
    kotlin("jvm") version "1.6.10"

    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"

    id("net.nemerosa.versioning") version "2.15.1"
    id("org.jetbrains.dokka") version "1.6.10"
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
}

group = "dev.forst"
base.archivesName.set("ktor-api-key")
version = (versioning.info?.tag ?: versioning.info?.lastTag ?: versioning.info?.build) ?: "SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    // Ktor server dependencies
    val ktorVersion = "1.6.7"
    compileOnly("io.ktor", "ktor-server-core", ktorVersion)
    compileOnly("io.ktor", "ktor-auth", ktorVersion)

    // testing
    testImplementation("io.ktor", "ktor-server-core", ktorVersion)
    testImplementation("io.ktor", "ktor-server-test-host", ktorVersion)
    testImplementation("io.ktor", "ktor-auth", ktorVersion)
    testImplementation("io.ktor", "ktor-jackson", ktorVersion)
    testImplementation(kotlin("test"))
    testImplementation(kotlin("stdlib-jdk8"))

    testImplementation("ch.qos.logback", "logback-classic", "1.3.0-alpha5") // logging framework for the tests

    val junitVersion = "5.8.2"
    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion) // junit testing framework
    testImplementation("org.junit.jupiter", "junit-jupiter-params", junitVersion) // generated parameters for tests
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion) // testing runtime
}

detekt {
    config = files("detekt.yml")
    parallel = true
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        useJUnitPlatform()
    }

    dokkaHtml {
        outputDirectory.set(File("$buildDir/docs"))

        dokkaSourceSets {
            configureEach {
                displayName.set("Ktor API Key Authentication Provider")

                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(URL("https://github.com/LukasForst/ktor-api-key/tree/master/src/main/kotlin"))
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }
}

// ------------------------------------ Deployment Configuration  ------------------------------------
// deployment configuration - deploy with sources and documentation
val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

// name the publication as it is referenced
val publication = "mavenJava"
publishing {
    // create jar with sources and with javadoc
    publications {
        create<MavenPublication>(publication) {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)

            pom {
                name.set("Ktor API Key Authentication Provider")
                description.set("Native API Key authentication in Ktor.")
                url.set("https://ktor-api-key.forst.dev")
                packaging = "jar"
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://mit-license.org/license.txt")
                    }
                }
                developers {
                    developer {
                        id.set("lukasforst")
                        name.set("Lukas Forst")
                        email.set("lukas@forst.dev")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/LukasForst/ktor-api-key.git")
                    url.set("https://github.com/LukasForst/ktor-api-key")
                }
            }
        }
    }
}

signing {
    val signingKeyId = project.findProperty("gpg.keyId") as String? ?: System.getenv("GPG_KEY_ID")
    val signingKey = project.findProperty("gpg.key") as String? ?: System.getenv("GPG_KEY")
    val signingPassword = project.findProperty("gpg.keyPassword") as String? ?: System.getenv("GPG_KEY_PASSWORD")

    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications[publication])
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(project.findProperty("ossrh.username") as String? ?: System.getenv("OSSRH_USERNAME"))
            password.set(project.findProperty("ossrh.password") as String? ?: System.getenv("OSSRH_PASSWORD"))
        }
    }
}
