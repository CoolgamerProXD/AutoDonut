# Offline type-check harness

`./gradlew build` is the real build. Use it whenever you can.

This directory is a fallback for environments where the Minecraft and Fabric
artifacts can't be downloaded. It contains hand-written stubs for every
external type AutoDonut touches, so the whole of `src/` can still be compiled
and type-checked.

```bash
ECJ_JAR=/path/to/ecj.jar ./tools/offline-typecheck/run.sh
```

## What this proves

* Every file in `src/` parses and type-checks.
* Generics, control flow, definite assignment, missing returns, unreachable
  code and switch exhaustiveness are all checked by a real Java compiler.
* Every call **between AutoDonut's own classes** resolves with correct types.
* Every external API member the mod uses is declared exactly once, in
  `stubs/`, so the full external surface is visible and reviewable.

## What this does NOT prove

The stubs encode our *assumption* about each Minecraft/Fabric signature. If a
stub is wrong, this harness compiles happily and `./gradlew build` will not.
`../../API-SURFACE.md` lists the entire external surface for exactly that
reason - it is the review checklist.

## Keeping it current

If you add a call to a Minecraft API that isn't stubbed yet, the compile fails
with "method X is undefined". Add the member to the matching file under
`stubs/`, matching the real signature as closely as you can, and note it in
`API-SURFACE.md`.
