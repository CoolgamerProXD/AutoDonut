#!/usr/bin/env bash
# Locate (or fetch) a Java runtime and an ECJ jar, then print them as
# JAVA_BIN=... and ECJ_JAR=... for run.sh to consume.
#
# Order of preference:
#   1. whatever is already on PATH / in $JAVA_HOME / $ECJ_JAR
#   2. a JRE and ECJ pulled from PyPI (jdk4py + karellen-jdtls), which works
#      even when Maven Central and Adoptium are unreachable
set -euo pipefail

find_java() {
  if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then echo "${JAVA_HOME}/bin/java"; return; fi
  if command -v java >/dev/null 2>&1; then command -v java; return; fi
  local p
  p="$(python3 -c 'import jdk4py,os;print(os.path.join(jdk4py.JAVA_HOME,"bin","java"))' 2>/dev/null || true)"
  if [[ -n "$p" && -x "$p" ]]; then echo "$p"; return; fi
  return 1
}

find_ecj() {
  if [[ -n "${ECJ_JAR:-}" && -f "${ECJ_JAR}" ]]; then echo "${ECJ_JAR}"; return; fi
  local p
  p="$(ls /usr/local/lib/karellen-jdtls*/plugins/org.eclipse.jdt.core.compiler.batch_*.jar 2>/dev/null | head -1 || true)"
  if [[ -n "$p" ]]; then echo "$p"; return; fi
  p="$(find / -name 'org.eclipse.jdt.core.compiler.batch_*.jar' -o -name 'ecj*.jar' 2>/dev/null | head -1 || true)"
  if [[ -n "$p" ]]; then echo "$p"; return; fi
  return 1
}

JAVA_BIN="$(find_java || true)"
ECJ_FOUND="$(find_ecj || true)"

if [[ -z "$JAVA_BIN" || -z "$ECJ_FOUND" ]]; then
  echo "bootstrap: fetching a JRE and ECJ from PyPI..." >&2
  pip install --quiet --break-system-packages jdk4py karellen-jdtls >&2 || \
    pip install --quiet jdk4py karellen-jdtls >&2
  rm -f /usr/local/lib/python3.11/dist-packages/karellen_jdtls-*.pth 2>/dev/null || true
  JAVA_BIN="$(find_java)"
  ECJ_FOUND="$(find_ecj)"
fi

echo "JAVA_BIN=${JAVA_BIN}"
echo "ECJ_JAR=${ECJ_FOUND}"
