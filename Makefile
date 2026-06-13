# Tessera developer entry points.
# On Windows without make, use: pwsh ./bootstrap.ps1  (or run the gradlew tasks directly).

.PHONY: bootstrap lint test build clean

bootstrap:
	@bash tools/bootstrap.sh

lint:
	./gradlew ktlintCheck detekt

test:
	./gradlew testFossDebugUnitTest koverXmlReport

build:
	./gradlew assembleFossDebug assemblePlayDebug

clean:
	./gradlew clean
