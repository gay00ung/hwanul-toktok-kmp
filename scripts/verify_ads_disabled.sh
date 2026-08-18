#!/usr/bin/env bash

set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_root"

search_targets=(
  composeApp/src
  composeApp/build.gradle.kts
  gradle/libs.versions.toml
  iosApp
)

source_files=()
while IFS= read -r -d '' file; do
  source_files+=("$file")
done < <(
  find "${search_targets[@]}" \
    -type f \
    \( \
      -name '*.kt' -o \
      -name '*.kts' -o \
      -name '*.toml' -o \
      -name '*.swift' -o \
      -name '*.plist' -o \
      -name '*.pbxproj' -o \
      -name '*.xcconfig' \
    \) \
    -not -path '*/build/*' \
    -not -path '*/.gradle/*' \
    -print0
)

if violations="$(grep \
  --line-number \
  --with-filename \
  --fixed-strings \
  -e 'com.google.android.gms:play-services-ads' \
  -e 'com.google.android.gms.ads.MobileAds' \
  -e 'com.google.android.gms.ads.AdView' \
  -e 'com.google.android.gms.ads.AdRequest' \
  -e 'com.google.android.gms.ads.interstitial' \
  -e 'GoogleMobileAds' \
  -e 'GADApplicationIdentifier' \
  -e 'ADMOB_' \
  -e 'ca-app-pub-' \
  "${source_files[@]}")"; then
  echo "Advertising is disabled for account safety. Remove these Mobile Ads markers:" >&2
  echo "$violations" >&2
  exit 1
else
  search_status=$?
  if [[ "$search_status" -ne 1 ]]; then
    exit "$search_status"
  fi
fi
