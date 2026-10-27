#!/usr/bin/env bash
# Copie la stack « chess » sur le VPS et la (re)construit.
#   ./deploy.sh          copie seulement (compose + sources dans projects/chess/)
#   ./deploy.sh up       copie puis docker compose up -d --build
#
# À lancer depuis le PC. Sources attendues côte à côte : ../../chessGame, ../../chessServer, ../../chessWeb.
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/../.." && pwd)"
STACK=/home/calixteair/docker/projects/chess
STAGING=/tmp/chess-stack
SUDO_PASS="$HOME/.config/vps-claude/sudo_pass"

# Le VPS n'a pas rsync : archive tar transmise par SSH vers un dossier de transit.
ssh vps-claude "rm -rf $STAGING && mkdir -p $STAGING/src"
tar -C "$ROOT" -czf - \
  --exclude=target --exclude=node_modules --exclude=dist --exclude=.git --exclude=.idea \
  --exclude=server.log --exclude='*.local' \
  chessGame chessServer chessWeb | ssh vps-claude "tar -xzf - -C $STAGING/src"
scp -q "$HERE/docker-compose.yml" "vps-claude:$STAGING/docker-compose.yml"

# Mise en place sous calixteair, compose non lisible par les autres (règle vps-secrets-compose-lisibles).
ssh vps-claude "sudo -S -p '' sh -c '
  mkdir -p $STACK &&
  rm -rf $STACK/src &&
  cp -a $STAGING/. $STACK/ &&
  chown -R calixteair:calixteair $STACK &&
  chmod 750 $STACK && chmod 640 $STACK/docker-compose.yml &&
  ln -sfn $STACK /home/calixteair/dockge-stacks/chess &&
  mkdir -p /etc/bao-deploy/no-secrets.d && touch /etc/bao-deploy/no-secrets.d/chess &&
  rm -rf $STAGING
'" < "$SUDO_PASS"

if [[ "${1:-}" == "up" ]]; then
  ssh vps-claude "sudo -S -p '' docker compose -f $STACK/docker-compose.yml up -d --build" < "$SUDO_PASS"
  ssh vps-claude "sudo -S -p '' docker compose -f $STACK/docker-compose.yml ps" < "$SUDO_PASS"
fi
