#!/usr/bin/env bash
# Checks that a WebGPU backend snapshot is a build of the Awake Core version it is paired with.
#
# Usage: check-webgpu-pin.sh <core> <webgpu>
#
# Reads the newest webgpu-wasm-js build of <webgpu> from Central's snapshot repository and every
# Awake dependency and constraint its wasmJs variants declare. Each must name <core> or
# <core>-SNAPSHOT: a build of a later Core main names a later version and would move part of Core
# up on the web build only, so it fails. Prints two lines on stdout:
#
#   strict=true|false  true for the release pin Core's tag run publishes, which holds every Core
#                      module at exactly <core> with strictly constraints; false for an older build
#                      whose Core edges are <core>-SNAPSHOT, which only matches the release if no
#                      Core change landed after the cut.
#   build=<timestamp>-<n>  the timestamped build it checked.
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <core> <webgpu>" >&2
  exit 2
fi
core=$1
webgpu=$2

if [[ "$webgpu" != *-SNAPSHOT ]]; then
  echo "::error::WebGPU $webgpu is not a snapshot; WebGPU publishes only snapshots while it builds on a wgpu4k snapshot." >&2
  exit 1
fi

repo=https://central.sonatype.com/repository/maven-snapshots/com/awakekt/awake/backend/webgpu-wasm-js/$webgpu
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

status=$(curl -sS --retry 3 -o "$work/maven-metadata.xml" -w '%{http_code}' "$repo/maven-metadata.xml")
if [[ "$status" != 200 ]]; then
  echo "::error::WebGPU $webgpu has no published build ($repo/maven-metadata.xml returned $status)." >&2
  exit 1
fi

# The metadata lists one <snapshotVersion> per file; take the value of the .module one, the file
# Gradle reads in preference to the POM.
value=$(tr -d '\r\n' < "$work/maven-metadata.xml" |
  sed 's:</snapshotVersion>:&\n:g' |
  grep '<extension>module</extension>' |
  grep -v '<classifier>' |
  sed -n 's:.*<value>\([^<]*\)</value>.*:\1:p' |
  head -n 1 || true)
if [[ -z "$value" ]]; then
  echo "::error::$repo/maven-metadata.xml lists no .module file." >&2
  exit 1
fi
build=${value#"${webgpu%-SNAPSHOT}-"}

curl -fsS --retry 3 -o "$work/module.json" "$repo/webgpu-wasm-js-$value.module"

# One line per Awake edge, fields split by |: kind, coordinate, then the strictly, requires and
# prefers versions. The WebGPU family itself is skipped; it is versioned as the pin, not as Core.
edges=$(jq -r '
  [.variants[] | select(.name | test("^wasmJs(Api|Runtime)Elements"))] as $variants
  | if ($variants | length) == 0 then error("no wasmJs variants") else . end
  | $variants[]
  | ((.dependencies // [])[] | {kind: "dependency"} + .),
    ((.dependencyConstraints // [])[] | {kind: "constraint"} + .)
  | select(.group | test("^com\\.awakekt\\.awake(\\.|$)"))
  | select((.group == "com.awakekt.awake.backend" and (.module | startswith("webgpu"))) | not)
  | [.kind, "\(.group):\(.module)", (.version.strictly // ""), (.version.requires // ""), (.version.prefers // "")]
  | join("|")
' "$work/module.json" | tr -d '\r' | sort -u)

if [[ "$core" == *-SNAPSHOT ]]; then
  allowed=("$core")
else
  allowed=("$core" "$core-SNAPSHOT")
fi

wrong=()
dependencies=0
constraints=0
exact=true
while IFS='|' read -r kind coordinate strictly requires prefers; do
  [[ -n "$kind" ]] || continue
  if [[ "$kind" == constraint ]]; then
    constraints=$((constraints + 1))
    [[ "$strictly" == "$core" ]] || exact=false
  else
    dependencies=$((dependencies + 1))
    [[ "${strictly:-$requires}" == "$core" ]] || exact=false
  fi
  for version in "$strictly" "$requires" "$prefers"; do
    [[ -n "$version" ]] || continue
    ok=false
    for candidate in "${allowed[@]}"; do
      if [[ "$version" == "$candidate" ]]; then ok=true; fi
    done
    [[ "$ok" == true ]] || wrong+=("$kind $coordinate:$version")
  done
done <<< "$edges"

echo "Checked webgpu-wasm-js $value: $dependencies Awake dependencies, $constraints Awake constraints." >&2
if [[ ${#wrong[@]} -gt 0 ]]; then
  echo "::error::WebGPU $webgpu (build $build) is not a build of Core $core. It names:" >&2
  printf '  %s\n' "${wrong[@]}" >&2
  exit 1
fi
if [[ "$dependencies" -eq 0 ]]; then
  echo "::error::WebGPU $webgpu (build $build) declares no Awake dependencies; its metadata is not the one this check reads." >&2
  exit 1
fi

strict=false
if [[ "$core" != *-SNAPSHOT && "$constraints" -gt 0 && "$exact" == true ]]; then
  strict=true
elif [[ "$core" != *-SNAPSHOT ]]; then
  echo "::warning::WebGPU $webgpu (build $build) predates Core's release pin: it holds no Core module strictly at $core, so it runs the release only if no Core change landed between this build and the cut." >&2
fi

echo "strict=$strict"
echo "build=$build"
