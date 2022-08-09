plugins {
    kotlin("jvm") version "1.6.10"

    id("net.nemerosa.versioning") version "3.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.20.0"
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
    detektPlugins("io.gitlab.arturbosch.detekt", "detekt-formatting", "1.20.0")
}

