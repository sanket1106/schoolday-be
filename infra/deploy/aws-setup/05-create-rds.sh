#!/bin/bash

# Create RDS MySQL database for SchoolDay
# This script creates an RDS instance with Multi-AZ deployment

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESOURCES_FILE="${SCRIPT_DIR}/aws-resources.env"

# Load previously created resources
if [ ! -f "$RESOURCES_FILE" ]; then
    echo "Error: ${RESOURCES_FILE} not found!"
    echo "Please run previous scripts first"
    exit 1
fi

source "$RESOURCES_FILE"

DB_INSTANCE_CLASS=${1:-db.t3.micro}
MULTI_AZ=${2:-true}

echo "======================================"
echo "Creating RDS MySQL Database"
echo "======================================"
echo "Region: ${AWS_REGION}"
echo "Instance Class: ${DB_INSTANCE_CLASS}"
echo "Multi-AZ: ${MULTI_AZ}"
echo "======================================"

# Get MySQL password from Secrets Manager
echo ""
echo "Step 1: Retrieving MySQL password from Secrets Manager..."
MYSQL_PASSWORD=$(aws secretsmanager get-secret-value \
  --secret-id ${MYSQL_PASSWORD_SECRET_NAME} \
  --region ${AWS_REGION} \
  --query 'SecretString' \
  --output text)

echo "✓ Password retrieved"

# Create DB subnet group
echo ""
echo "Step 2: Creating DB subnet group..."
aws rds create-db-subnet-group \
  --db-subnet-group-name schoolday-db-subnet-group \
  --db-subnet-group-description "Subnet group for SchoolDay RDS" \
  --subnet-ids ${PRIVATE_SUBNET_1} ${PRIVATE_SUBNET_2} \
  --region ${AWS_REGION} \
  > /dev/null 2>&1 || echo "  (Subnet group may already exist)"

echo "✓ DB subnet group ready"

# Create RDS instance
echo ""
echo "Step 3: Creating RDS instance..."
echo "  This will take 5-10 minutes..."

aws rds create-db-instance \
  --db-instance-identifier schoolday-db \
  --db-instance-class ${DB_INSTANCE_CLASS} \
  --engine mysql \
  --engine-version 8.0.35 \
  --master-username root \
  --master-user-password "${MYSQL_PASSWORD}" \
  --allocated-storage 20 \
  --storage-type gp3 \
  --vpc-security-group-ids ${RDS_SG} \
  --db-subnet-group-name schoolday-db-subnet-group \
  --backup-retention-period 7 \
  --preferred-backup-window "03:00-04:00" \
  --preferred-maintenance-window "mon:04:00-mon:05:00" \
  --multi-az \
  --publicly-accessible false \
  --storage-encrypted \
  --enable-cloudwatch-logs-exports error general slowquery \
  --tags Key=Name,Value=schoolday-db Key=Environment,Value=production \
  --region ${AWS_REGION} \
  > /dev/null

echo "✓ RDS instance creation initiated"

# Wait for instance to be available
echo ""
echo "Step 4: Waiting for RDS instance to become available..."
echo "  This may take 5-10 minutes. Please be patient..."
aws rds wait db-instance-available \
  --db-instance-identifier schoolday-db \
  --region ${AWS_REGION}

echo "✓ RDS instance is now available"

# Get RDS endpoint
RDS_ENDPOINT=$(aws rds describe-db-instances \
  --db-instance-identifier schoolday-db \
  --region ${AWS_REGION} \
  --query 'DBInstances[0].Endpoint.Address' \
  --output text)

echo "✓ RDS Endpoint: ${RDS_ENDPOINT}"

# Append RDS info to resources file
echo ""
echo "Step 5: Saving RDS endpoint..."
cat >> ${RESOURCES_FILE} <<EOF

# RDS Database
export RDS_ENDPOINT="${RDS_ENDPOINT}"
export RDS_PORT="3306"
export RDS_DATABASE="schoolday"
EOF

echo "✓ RDS info appended to ${RESOURCES_FILE}"

echo ""
echo "======================================"
echo "✓ RDS Database Created Successfully!"
echo "======================================"
echo ""
echo "Summary:"
echo "  DB Instance: schoolday-db"
echo "  Endpoint: ${RDS_ENDPOINT}"
echo "  Port: 3306"
echo "  Database: schoolday"
echo "  Multi-AZ: ${MULTI_AZ}"
echo ""
echo "IMPORTANT: You need to initialize the database schema."
echo "See the documentation for schema initialization steps."
echo ""
echo "Next step: Run ./06-create-alb.sh"
