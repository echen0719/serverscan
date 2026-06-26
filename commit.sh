#!/bin/bash

COMMIT=""

branches=(
  "26.2"
  "26.1-26.1.2"
  "1.21.9-1.21.11"
  "1.21.6-1.21.8"
  "1.21.5"
  "1.21-1.21.4"
)

for branch in "${branches[@]}"; do
  echo "Processing branch: $branch"

  git checkout "$branch"
  git cherry-pick "$COMMIT"
  git push origin "$branch"

  echo -e "Complete\n"
done

echo "Done""