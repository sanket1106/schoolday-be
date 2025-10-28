#!/bin/bash

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Load environment variables from .env file
ENV_FILE="$SCRIPT_DIR/../.env"

if [ -f "$ENV_FILE" ]; then
  source "$ENV_FILE"
else
  echo "Error: .env file not found at $ENV_FILE"
  echo "Please create it from .env.example and configure your MySQL password"
  exit 1
fi

# Database credentials from environment
DB_USER="root"
DB_PASS="${MYSQL_ROOT_PASSWORD}"
DB_NAME="${MYSQL_DATABASE:-schoolday}"
MYSQL_HOST="${MYSQL_HOST:-localhost}"

# Path to the SQL file
SQL_SCRIPT="${SCRIPT_DIR}/sql/basedata.sql"

# Check if the SQL script exists
if [ ! -f "$SQL_SCRIPT" ]; then
    echo "Error: SQL script '$SQL_SCRIPT' not found!"
    exit 1
fi

# Execute the SQL script using MySQL client
echo "Executing SQL script: $SQL_SCRIPT"

cat "$SQL_SCRIPT" | docker exec -i mysql8 sh -c "mysql -u$DB_USER -p$DB_PASS"
#docker exec -it mysql8 mysql --batch --silent -h "$MYSQL_HOST" -u "$DB_USER" -p"$DB_PASS" < "$SQL_SCRIPT"

# Check if the command was successful
if [ $? -eq 0 ]; then
    echo "SQL script executed successfully!"
else
    echo "Error executing SQL script!"
    exit 1
fi