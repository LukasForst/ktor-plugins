detekt:
	./gradlew detekt

detekt-correct:
	./gradlew detekt --auto-correct || true
	./gradlew detekt

test:
	./gradlew test

check: detekt test

release:
	./gradlew clean assemble publishToSonatype closeAndReleaseSonatypeStagingRepository
