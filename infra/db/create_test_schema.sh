#!/bin/bash

# Test database setup script
# This script creates the test database and applies the schema

# Database configuration
DB_NAME="schoolday_test"
DB_USER="root"
DB_PASSWORD="rootpassword"
CONTAINER_NAME="mysql8"

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# SQL script paths
SCHEMA_SCRIPT="${SCRIPT_DIR}/sql/schema_test.sql"
BASE_DATA_SCRIPT="${SCRIPT_DIR}/sql/basedata_test.sql"

echo "Creating test database: $DB_NAME"

# Create the test database
docker exec -i "$CONTAINER_NAME" mysql -u"$DB_USER" -p"$DB_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME;"

if [ $? -eq 0 ]; then
    echo "Test database created successfully!"
else
    echo "Error creating test database!"
    exit 1
fi

echo "Executing SQL script: $SCHEMA_SCRIPT"
docker exec -i "$CONTAINER_NAME" mysql -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$SCHEMA_SCRIPT"

if [ $? -eq 0 ]; then
    echo "Schema applied successfully!"
else
    echo "Error executing SQL script for test database!"
    exit 1
fi

echo "Executing base data script: $BASE_DATA_SCRIPT"
docker exec -i "$CONTAINER_NAME" mysql -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$BASE_DATA_SCRIPT"

if [ $? -eq 0 ]; then
    echo "Base data applied successfully!"
    echo "Test database setup completed successfully!"
else
    echo "Error executing base data script for test database!"
    exit 1
fi 