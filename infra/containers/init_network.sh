NETWORK_NAME=$1
if ! podman network exists schoolday-network; then
  podman network create $NETWORK_NAME
fi
