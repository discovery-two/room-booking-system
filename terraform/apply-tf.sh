#!/usr/bin/env bash

set -euo pipefail

if [ -f .env ]; then
  set -a
  . .env
else
  echo "Error: .env file not found."
  exit 1
fi

terraform fmt
terraform validate
terraform apply