#!/bin/bash

# Create secrets in AWS Secrets Manager
# This script stores the MySQL password securely

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESOURCES_FILE="${SCRIPT_DIR}/aws-resources.env"

# Load previously created resources
if [ ! -f "$RESOURCES_FILE" ]; then
    echo "Error: ${RESOURCES_FILE} not found!"
    echo "Please run ./01-create-vpc.sh first"
    exit 1
fi

source "$RESOURCES_FILE"

echo "======================================"
echo "Creating Secrets in AWS Secrets Manager"
echo "======================================"
echo "Region: ${AWS_REGION}"
echo "======================================"

# Prompt for MySQL password
echo ""
echo "Step 1: Getting MySQL password..."
read -sp "Enter MySQL root password: " MYSQL_PASSWORD
echo ""

if [ -z "$MYSQL_PASSWORD" ]; then
    echo "Error: Password cannot be empty"
    exit 1
fi

# Confirm password
read -sp "Confirm MySQL root password: " MYSQL_PASSWORD_CONFIRM
echo ""

if [ "$MYSQL_PASSWORD" != "$MYSQL_PASSWORD_CONFIRM" ]; then
    echo "Error: Passwords do not match"
    exit 1
fi

# Create secret
echo ""
echo "Step 2: Creating secret in Secrets Manager..."

SECRET_NAME="schoolday/mysql-password"

# Check if secret already exists
if aws secretsmanager describe-secret --secret-id ${SECRET_NAME} --region ${AWS_REGION} > /dev/null 2>&1; then
    echo "Secret already exists. Updating..."

    aws secretsmanager update-secret \
      --secret-id ${SECRET_NAME} \
      --secret-string "${MYSQL_PASSWORD}" \
      --region ${AWS_REGION} \
      > /dev/null

    echo "✓ Secret updated successfully"
else
    aws secretsmanager create-secret \
      --name ${SECRET_NAME} \
      --description "MySQL root password for SchoolDay database" \
      --secret-string "${MYSQL_PASSWORD}" \
      --region ${AWS_REGION} \
      > /dev/null

    echo "✓ Secret created successfully"
fi

# Get secret ARN
SECRET_ARN=$(aws secretsmanager describe-secret \
  --secret-id ${SECRET_NAME} \
  --region ${AWS_REGION} \
  --query 'ARN' \
  --output text)

echo "  Secret ARN: ${SECRET_ARN}"

# Append secret info to resources file
echo ""
echo "Step 3: Saving secret ARN..."
cat >> ${RESOURCES_FILE} <<EOF

# Secrets Manager
export MYSQL_PASSWORD_SECRET_NAME="${SECRET_NAME}"
export MYSQL_PASSWORD_SECRET_ARN="${SECRET_ARN}"
EOF

echo "✓ Secret ARN appended to ${RESOURCES_FILE}"

echo ""
echo "======================================"
echo "✓ Secrets Created Successfully!"
echo "======================================"
echo ""
echo "Summary:"
echo "  Secret Name: ${SECRET_NAME}"
echo "  Secret ARN: ${SECRET_ARN}"
echo ""
echo "Next step: Run ./05-create-rds.sh"
