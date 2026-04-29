#!/usr/bin/env bash
set -euo pipefail

PORT="${1:-8080}"

if ! [[ "$PORT" =~ ^[0-9]+$ ]] || (( PORT < 1 || PORT > 65535 )); then
    echo "Usage: $0 <PORT>   (port must be an integer in 1..65535)" >&2
    exit 1
fi

export PORT

exec docker compose up --build --remove-orphan