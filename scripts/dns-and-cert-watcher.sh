#!/usr/bin/env bash
set -euo pipefail

HOSTINGER_TOKEN="IhrRT6bdDONsFoNGtasGlg4C5p2FRUHpAWMUBao662ec8c22"
VPS_IP="145.14.157.205"
DOMAIN="ugogbe.info"
HOSTS=("smp.ugogbe.info" "xftp.ugogbe.info")
VPS_KEY="/root/.ssh/id_ikoro_vps"
VPS_USER="root"
VPS_HOST="145.14.157.205"

log() { echo "[$(date -u +%Y-%m-%dT%H:%M:%SZ)] $*"; }

resolved=0
for h in "${HOSTS[@]}"; do
  ip=$(dig +short A "$h" @8.8.8.8 | head -n1 || true)
  log "$h resolves to: $ip"
  if [ "$ip" != "$VPS_IP" ]; then
    resolved=1
  fi
done

if [ "$resolved" -ne 0 ]; then
  log "DNS not ready yet; will retry."
  exit 0
fi

log "DNS propagated. Issuing certificates..."

# Run certbot on VPS via SSH
ssh -o StrictHostKeyChecking=no -i "$VPS_KEY" "${VPS_USER}@${VPS_HOST}" <<'REMOTE'
  set -e
  snap install certbot --classic 2>/dev/null || true
  certbot certonly --standalone -d smp.ugogbe.info -d xftp.ugogbe.info --agree-tos --non-interactive --email cjibemere@gmail.com || true
  ln -sf /etc/letsencrypt/live/smp.ugogbe.info/fullchain.pem /opt/simplex/certs/cert.pem || true
  ln -sf /etc/letsencrypt/live/smp.ugogbe.info/privkey.pem /opt/simplex/certs/key.pem || true
  cat /opt/simplex/certs/cert.pem /opt/simplex/certs/key.pem > /opt/simplex/certs/combined.pem || true
  systemctl reload nginx || true
  cd /opt/simplex && docker compose down && docker compose up -d
REMOTE

log "Pushing A records to Hostinger if zone active..."
for record in smp xftp; do
  curl -s -X PUT "https://developers.hostinger.com/api/dns/v1/zones/${DOMAIN}" \
    -H "Authorization: Bearer ${HOSTINGER_TOKEN}" \
    -H "Content-Type: application/json" \
    -H "User-Agent: curl/8.0.0" \
    -d "{\"domain\":\"${DOMAIN}\",\"overwrite\":false,\"zone\":[{\"type\":\"A\",\"name\":\"${record}\",\"points_to\":\"${VPS_IP}\",\"ttl\":3600}]}" || true
done

log "Cert + DNS setup complete."
