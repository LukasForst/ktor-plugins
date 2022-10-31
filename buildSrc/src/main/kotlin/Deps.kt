object Versions {
    const val detekt = "1.20.0"
    const val ktor = "2.1.3"
    const val jupiterVersion = "5.9.0"
}

object Libs {

    const val ktorServerCore = "io.ktor:ktor-server-core:${Versions.ktor}"
    const val ktorAuth = "io.ktor:ktor-server-auth:${Versions.ktor}"
    const val ktorContentNegotation = "io.ktor:ktor-server-content-negotiation:${Versions.ktor}"
    const val ktorSerializationJackson = "io.ktor:ktor-serialization-jackson:${Versions.ktor}"

    const val ktorClientContentNegotiation = "io.ktor:ktor-client-content-negotiation:${Versions.ktor}"


    object Test {
        const val logBack = "ch.qos.logback:logback-classic:1.3.0-beta0"

        const val ktorServerTestHost = "io.ktor:ktor-server-test-host:${Versions.ktor}"

        const val mockk = "io.mockk:mockk:1.12.5"

        const val jupiterApi = "org.junit.jupiter:junit-jupiter-api:${Versions.jupiterVersion}"
        const val jupiterParams = "org.junit.jupiter:junit-jupiter-params:${Versions.jupiterVersion}"
        const val jupiterRuntime = "org.junit.jupiter:junit-jupiter-engine:${Versions.jupiterVersion}"
    }
}
