#!/usr/bin/env bash
set -euo pipefail

REMOTE_USER="${REMOTE_USER:-agungdh}"
REMOTE_HOST="${REMOTE_HOST:-172.17.0.1}"
RETRY_DELAY="${RETRY_DELAY:-3}"

PORTS=(5432 6379 9000 9001 8083 8084)

exec 9>"${TMPDIR:-/tmp}/docker-compose-forward.lock"
flock -n 9 || { echo "forwarder already running" >&2; exit 1; }

forward_args=()
for port in "${PORTS[@]}"; do
  forward_args+=(-L "127.0.0.1:${port}:127.0.0.1:${port}")
done

trap 'exit 0' INT TERM

while true; do
  echo "[$(date '+%F %T')] connecting ${REMOTE_USER}@${REMOTE_HOST}"
  ssh -N -T \
    -o ExitOnForwardFailure=yes \
    -o ServerAliveInterval=15 \
    -o ServerAliveCountMax=3 \
    -o TCPKeepAlive=yes \
    -o ConnectTimeout=10 \
    -o StrictHostKeyChecking=accept-new \
    "${forward_args[@]}" \
    "${REMOTE_USER}@${REMOTE_HOST}" || true
  echo "[$(date '+%F %T')] disconnected, retry in ${RETRY_DELAY}s"
  sleep "$RETRY_DELAY"
done
