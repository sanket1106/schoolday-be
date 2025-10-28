POD_NAME=$1
NETWORK_NAME=$2

# Load environment variables from .env file
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/../.env"

if [ -f "$ENV_FILE" ]; then
  source "$ENV_FILE"
else
  echo "Error: .env file not found at $ENV_FILE"
  echo "Please create it from .env.example and configure your MySQL password"
  exit 1
fi

MYSQL_DATABASE="${MYSQL_DATABASE:-schoolday}"
MYSQL_USER="app_user"
MYSQL_VERSION="8"

if ! podman container exists $POD_NAME; then
  echo "Creating MySQL container..."
  podman create \
    --name $POD_NAME \
    --network $NETWORK_NAME \
    -e MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD \
    -e MYSQL_DATABASE=$MYSQL_DATABASE \
    -p 3306:3306 \
    -v mysql-data:/var/lib/mysql \
    mysql:8
else
  echo "MySQL container already exists."
fi

podman ps -a
