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
# bootstrap.sh finds a JVM and an ECJ jar, fetching them from PyPI if needed,
# so this works on a machine with no JDK at all.
set -euo pipefail
cd "$(dirname "$0")/../.."

eval "$(bash tools/offline-typecheck/bootstrap.sh)"
if [[ -z "${JAVA_BIN:-}" || -z "${ECJ_JAR:-}" ]]; then
  echo "error: could not find a JRE and an ECJ jar" >&2
  exit 2
fi

OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT

mapfile -t SRCS < <(find src -name '*.java')
mapfile -t STUBS < <(find tools/offline-typecheck/stubs -name '*.java')

echo "type-checking ${#SRCS[@]} project sources against ${#STUBS[@]} API stubs..."
"$JAVA_BIN" -jar "$ECJ_JAR" -21 -nowarn -proc:none -d "$OUT" "${SRCS[@]}" "${STUBS[@]}"
echo "OK - no type errors in src/"
