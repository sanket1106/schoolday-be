#!/bin/bash

# Database credentials
DB_USER="root"
DB_PASS="rootpassword"
DB_NAME="schoolday"

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Path to the SQL file
SQL_SCRIPT="${SCRIPT_DIR}/sql/basedata.sql"

# MySQL host (default is localhost)
MYSQL_HOST="localhost"

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