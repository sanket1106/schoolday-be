#!/bin/bash

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Path to the test database setup script (now in the same directory)
TEST_DB_SCRIPT="${SCRIPT_DIR}/create_test_schema.sh"

# Check if the test database script exists
if [ ! -f "$TEST_DB_SCRIPT" ]; then
    echo "Error: Test database script '$TEST_DB_SCRIPT' not found!"
    exit 1
fi

# Make sure the script is executable
chmod +x "$TEST_DB_SCRIPT"

# Execute the test database setup script
echo "Setting up test database..."
"$TEST_DB_SCRIPT"

# Check if the command was successful
if [ $? -eq 0 ]; then
    echo "Test database setup completed successfully!"
    echo "You can now run tests with: ./mvnw test"
else
    echo "Error setting up test database!"
    exit 1
fi 