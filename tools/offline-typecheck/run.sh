#!/usr/bin/env bash
# Offline type-check for AutoDonut.
#
# `./gradlew build` is the authoritative build and you should always prefer it.
# This script exists for the case where the real Minecraft / Fabric artifacts
# cannot be downloaded (no network, blocked Maven, CI outage). It compiles every
# file in src/ against the hand-written API stubs in ./stubs, which type-checks
# all of AutoDonut's own code - syntax, generics, control flow, name resolution,
# and every call between AutoDonut's own classes.
#
# What it does NOT do: confirm that the stub signatures match real Minecraft.
# See API-SURFACE.md for the full list of external members that assumption covers.
#
# Requires: a JVM (java 17+) and ecj.jar (Eclipse Compiler for Java) on PATH or
# pointed at by $ECJ_JAR.
set -euo pipefail
cd "$(dirname "$0")/../.."

ECJ_JAR="${ECJ_JAR:-$(ls "$(dirname "$0")"/ecj*.jar 2>/dev/null | head -1 || true)}"
if [[ -z "${ECJ_JAR}" || ! -f "${ECJ_JAR}" ]]; then
  echo "error: set ECJ_JAR to an Eclipse batch compiler jar" >&2
  echo "  (org.eclipse.jdt.core.compiler.batch, e.g. from Maven Central)" >&2
  exit 2
fi

OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT

mapfile -t SRCS < <(find src -name '*.java')
mapfile -t STUBS < <(find tools/offline-typecheck/stubs -name '*.java')

echo "type-checking ${#SRCS[@]} project sources against ${#STUBS[@]} API stubs..."
java -jar "$ECJ_JAR" -21 -nowarn -proc:none -d "$OUT" "${SRCS[@]}" "${STUBS[@]}"
echo "OK - no type errors in src/"
