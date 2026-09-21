#!/usr/bin/env sh

set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

fail() {
	printf 'Error: %s\n' "$1" >&2
	exit 1
}

command -v java >/dev/null 2>&1 || fail 'Java was not found in PATH. Install Java 21 and try again.'
command -v node >/dev/null 2>&1 || fail 'Node.js was not found in PATH. Install Node.js and try again.'
command -v npm >/dev/null 2>&1 || fail 'npm was not found in PATH. Install npm and try again.'

JAVA_VERSION=$(java -version 2>&1 | sed -n 's/.*version "\([0-9][0-9]*\).*"/\1/p' | head -n 1)
[ "$JAVA_VERSION" = '21' ] || fail "Java 21 is required; detected version: ${JAVA_VERSION:-unknown}."

printf 'Java detected: '
java -version 2>&1 | head -n 1
printf 'Node.js detected: %s\n' "$(node --version)"
printf 'npm detected: %s\n' "$(npm --version)"

printf '\nInstalling frontend dependencies with npm ci...\n'
(
	cd "$ROOT_DIR/frontend"
	npm ci
)

printf '\nPreparing backend dependencies with the Maven Wrapper...\n'
(
	cd "$ROOT_DIR"
	sh backend/mvnw -q -DskipTests dependency:go-offline
)

printf '\nSetup completed successfully.\n'
