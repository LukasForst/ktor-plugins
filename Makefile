detekt:
	./gradlew detekt

test:
	./gradlew test

check: detekt test

release:
	./gradlew clean assemble publishToSonatype closeAndReleaseSonatypeStagingRepository
