description = "Ktor Api Key Provider"

dependencies {
    compileOnly(Libs.ktorAuth)

    testImplementation(Libs.ktorAuth)
    testImplementation(Libs.ktorContentNegotation)
    testImplementation(Libs.ktorSerializationJackson)
    testImplementation(Libs.ktorClientContentNegotiation)
}