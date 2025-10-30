#!/bin/bash

# Master setup script for AWS ECS deployment
# This script orchestrates all setup scripts in the correct order

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AWS_REGION=${1:-us-east-1}

echo "=========================================="
echo "SchoolDay AWS ECS Complete Setup"
echo "=========================================="
echo "This script will set up the entire AWS infrastructure"
echo "for deploying SchoolDay backend to ECS Fargate."
echo ""
echo "The following will be created:"
echo "  - VPC with public/private subnets"
echo "  - Security groups for ALB, ECS, and RDS"
echo "  - IAM roles for ECS tasks"
echo "  - Secrets in AWS Secrets Manager"
echo "  - RDS MySQL database"
echo "  - Application Load Balancer"
echo "  - ECS Cluster and Service"
echo ""
echo "Region: ${AWS_REGION}"
echo "=========================================="
echo ""

read -p "Do you want to continue? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo "Setup cancelled."
    exit 0
fi

echo ""
echo "Starting setup..."
echo ""

# Step 1: VPC and Networking
echo "===== STEP 1/7: Creating VPC and Networking ====="
${SCRIPT_DIR}/01-create-vpc.sh ${AWS_REGION}
echo ""

# Load resources
source ${SCRIPT_DIR}/aws-resources.env

# Step 2: Security Groups
echo "===== STEP 2/7: Creating Security Groups ====="
${SCRIPT_DIR}/02-create-security-groups.sh
echo ""

# Reload resources
source ${SCRIPT_DIR}/aws-resources.env

# Step 3: IAM Roles
echo "===== STEP 3/7: Creating IAM Roles ====="
${SCRIPT_DIR}/03-create-iam-roles.sh
echo ""

# Reload resources
source ${SCRIPT_DIR}/aws-resources.env

# Step 4: Secrets Manager
echo "===== STEP 4/7: Creating Secrets ====="
${SCRIPT_DIR}/04-create-secrets.sh
echo ""

# Reload resources
source ${SCRIPT_DIR}/aws-resources.env

# Step 5: RDS Database
echo "===== STEP 5/7: Creating RDS Database ====="
echo "Creating RDS with db.t3.micro (change in script for production)"
${SCRIPT_DIR}/05-create-rds.sh db.t3.micro true
echo ""

# Reload resources
source ${SCRIPT_DIR}/aws-resources.env

# Step 6: Application Load Balancer
echo "===== STEP 6/7: Creating Application Load Balancer ====="
${SCRIPT_DIR}/06-create-alb.sh
echo ""

# Reload resources
source ${SCRIPT_DIR}/aws-resources.env

# Step 7: ECS Cluster and Service
echo "===== STEP 7/7: Creating ECS Cluster and Service ====="
echo ""
echo "NOTE: You need to build and push your Docker image to ECR first!"
echo "Run: ./build-and-push.sh ${AWS_ACCOUNT_ID} ${AWS_REGION} latest"
echo ""
read -p "Have you pushed your Docker image to ECR? (yes/skip): " ECR_READY

if [ "$ECR_READY" = "yes" ]; then
    ECR_IMAGE="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/schoolday-app:latest"
    ${SCRIPT_DIR}/07-create-ecs.sh ${ECR_IMAGE} 2
else
    echo "Skipping ECS service creation."
    echo "Run ./07-create-ecs.sh manually after pushing your image."
fi

echo ""
echo "=========================================="
echo "✓ AWS Infrastructure Setup Complete!"
echo "=========================================="
echo ""
echo "All resource IDs are saved in: ${SCRIPT_DIR}/aws-resources.env"
echo ""
echo "Next steps:"
echo "1. Build and push Docker image (if not done):"
echo "   cd ${SCRIPT_DIR}/.."
echo "   ./build-and-push.sh ${AWS_ACCOUNT_ID} ${AWS_REGION} latest"
echo ""
echo "2. Create ECS service (if skipped):"
echo "   cd ${SCRIPT_DIR}"
echo "   ./07-create-ecs.sh"
echo ""
echo "3. Initialize database schema:"
echo "   See AWS_ECS_DEPLOYMENT.md for instructions"
echo ""
echo "4. Access your application:"
echo "   http://${ALB_DNS}"
echo ""
echo "=========================================="
