import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("jvm") version "1.8.10"

    `maven-publish`
    signing

    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    // we can not switch to 3.x.x because we want to keep it compatible with JVM 8
    id("net.nemerosa.versioning") version "2.15.1"
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
}

group = "dev.forst"
version = (versioning.info?.tag ?: versioning.info?.lastTag ?: versioning.info?.build) ?: "SNAPSHOT"

repositories {
    mavenCentral()
}

detekt {
    config = files("detekt.yml")
    parallel = true
    source = files(subprojects.map { it.projectDir })
}

dependencies {
    // detekt plugins, not present in the final build
    detektPlugins("io.gitlab.arturbosch.detekt", "detekt-formatting", "1.21.0")
}

tasks {
    withType<Detekt> {
        exclude("build.gradle.kts") // do not validate build scripts
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    apply {
        plugin("kotlin")
        plugin("maven-publish")
        plugin("signing")
    }

    base.archivesName.set(project.name)

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
    }

    dependencies {
        // do not include standard library by default
        compileOnly(kotlin("stdlib-jdk8"))

        // ktor server dependencies
        compileOnly(Libs.ktorServerCore)

        // testing, by default include ktor core
        testImplementation(Libs.ktorServerCore)
        testImplementation(Libs.Test.ktorServerTestHost)

        testImplementation(kotlin("test"))
        testImplementation(kotlin("stdlib-jdk8"))

        testImplementation(Libs.Test.logBack)

        testImplementation(Libs.Test.jupiterApi)
        testImplementation(Libs.Test.jupiterParams)
        testRuntimeOnly(Libs.Test.jupiterRuntime)
    }

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
    val publication = "defaultMavenJava"
    publishing {
        // create jar with sources and with javadoc
        publications {
            create<MavenPublication>(publication) {
                from(components["java"])
                artifact(sourcesJar)
                artifact(javadocJar)

                pom {
                    name.set(project.name)
                    description.set("${project.name} is part of Ktor plugins project developed by Lukas Forst.")
                    url.set("https://ktor-plugins.forst.dev")
                    packaging = "jar"

                    licenses {
                        license {
                            name.set("Apache-2.0")
                            url.set("https://github.com/LukasForst/ktor-plugins/blob/master/LICENSE")
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
                        connection.set("scm:git:git://github.com/LukasForst/ktor-plugins.git")
                        url.set("https://github.com/LukasForst/ktor-plugins")
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
