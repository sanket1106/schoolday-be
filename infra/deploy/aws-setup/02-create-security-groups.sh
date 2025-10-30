#!/bin/bash

# Create Security Groups for SchoolDay ECS deployment
# This script creates security groups for ALB, ECS, and RDS

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
echo "Creating Security Groups"
echo "======================================"
echo "Region: ${AWS_REGION}"
echo "VPC ID: ${VPC_ID}"
echo "======================================"

# Security Group for ALB (allows HTTP/HTTPS from internet)
echo ""
echo "Step 1: Creating ALB Security Group..."
ALB_SG=$(aws ec2 create-security-group \
  --group-name schoolday-alb-sg \
  --description "Security group for SchoolDay ALB" \
  --vpc-id ${VPC_ID} \
  --region ${AWS_REGION} \
  --query 'GroupId' \
  --output text)

echo "✓ ALB Security Group Created: ${ALB_SG}"

# Allow HTTP traffic
aws ec2 authorize-security-group-ingress \
  --group-id ${ALB_SG} \
  --protocol tcp \
  --port 80 \
  --cidr 0.0.0.0/0 \
  --region ${AWS_REGION}

echo "✓ HTTP (port 80) allowed from internet"

# Allow HTTPS traffic
aws ec2 authorize-security-group-ingress \
  --group-id ${ALB_SG} \
  --protocol tcp \
  --port 443 \
  --cidr 0.0.0.0/0 \
  --region ${AWS_REGION}

echo "✓ HTTPS (port 443) allowed from internet"

# Tag the security group
aws ec2 create-tags \
  --resources ${ALB_SG} \
  --tags Key=Name,Value=schoolday-alb-sg \
  --region ${AWS_REGION}

# Security Group for ECS Tasks (allows traffic from ALB)
echo ""
echo "Step 2: Creating ECS Security Group..."
ECS_SG=$(aws ec2 create-security-group \
  --group-name schoolday-ecs-sg \
  --description "Security group for SchoolDay ECS tasks" \
  --vpc-id ${VPC_ID} \
  --region ${AWS_REGION} \
  --query 'GroupId' \
  --output text)

echo "✓ ECS Security Group Created: ${ECS_SG}"

# Allow traffic from ALB on port 8081
aws ec2 authorize-security-group-ingress \
  --group-id ${ECS_SG} \
  --protocol tcp \
  --port 8081 \
  --source-group ${ALB_SG} \
  --region ${AWS_REGION}

echo "✓ Traffic from ALB allowed on port 8081"

# Tag the security group
aws ec2 create-tags \
  --resources ${ECS_SG} \
  --tags Key=Name,Value=schoolday-ecs-sg \
  --region ${AWS_REGION}

# Security Group for RDS (allows traffic from ECS)
echo ""
echo "Step 3: Creating RDS Security Group..."
RDS_SG=$(aws ec2 create-security-group \
  --group-name schoolday-rds-sg \
  --description "Security group for SchoolDay RDS" \
  --vpc-id ${VPC_ID} \
  --region ${AWS_REGION} \
  --query 'GroupId' \
  --output text)

echo "✓ RDS Security Group Created: ${RDS_SG}"

# Allow MySQL traffic from ECS
aws ec2 authorize-security-group-ingress \
  --group-id ${RDS_SG} \
  --protocol tcp \
  --port 3306 \
  --source-group ${ECS_SG} \
  --region ${AWS_REGION}

echo "✓ MySQL traffic from ECS allowed on port 3306"

# Tag the security group
aws ec2 create-tags \
  --resources ${RDS_SG} \
  --tags Key=Name,Value=schoolday-rds-sg \
  --region ${AWS_REGION}

# Append security group IDs to resources file
echo ""
echo "Step 4: Saving security group IDs..."
cat >> ${RESOURCES_FILE} <<EOF

# Security Groups
export ALB_SG="${ALB_SG}"
export ECS_SG="${ECS_SG}"
export RDS_SG="${RDS_SG}"
EOF

echo "✓ Security group IDs appended to ${RESOURCES_FILE}"

echo ""
echo "======================================"
echo "✓ Security Groups Created Successfully!"
echo "======================================"
echo ""
echo "Summary:"
echo "  ALB Security Group: ${ALB_SG}"
echo "  ECS Security Group: ${ECS_SG}"
echo "  RDS Security Group: ${RDS_SG}"
echo ""
echo "Next step: Run ./03-create-iam-roles.sh"
