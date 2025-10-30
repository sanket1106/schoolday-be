#!/bin/bash

# Create IAM Roles for ECS Tasks
# This script creates the necessary IAM roles for ECS task execution and task runtime

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

AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

echo "======================================"
echo "Creating IAM Roles"
echo "======================================"
echo "Region: ${AWS_REGION}"
echo "Account ID: ${AWS_ACCOUNT_ID}"
echo "======================================"

# Create trust policy document for ECS tasks
echo ""
echo "Step 1: Creating trust policy documents..."
cat > /tmp/ecs-task-trust-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ecs-tasks.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF

echo "✓ Trust policy document created"

# Create ECS Task Execution Role
echo ""
echo "Step 2: Creating ECS Task Execution Role..."

# Check if role already exists
if aws iam get-role --role-name ecsTaskExecutionRole > /dev/null 2>&1; then
    echo "✓ ecsTaskExecutionRole already exists, skipping creation"
    ECS_TASK_EXECUTION_ROLE_ARN=$(aws iam get-role --role-name ecsTaskExecutionRole --query 'Role.Arn' --output text)
else
    aws iam create-role \
      --role-name ecsTaskExecutionRole \
      --assume-role-policy-document file:///tmp/ecs-task-trust-policy.json \
      --description "Allows ECS tasks to call AWS services on your behalf" \
      > /dev/null

    echo "✓ ecsTaskExecutionRole created"

    # Attach AWS managed policy for ECS task execution
    aws iam attach-role-policy \
      --role-name ecsTaskExecutionRole \
      --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

    echo "✓ AmazonECSTaskExecutionRolePolicy attached"

    # Create and attach policy for Secrets Manager access
    cat > /tmp/ecs-secrets-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": "arn:aws:secretsmanager:${AWS_REGION}:${AWS_ACCOUNT_ID}:secret:schoolday/*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "kms:Decrypt"
      ],
      "Resource": "*",
      "Condition": {
        "StringEquals": {
          "kms:ViaService": "secretsmanager.${AWS_REGION}.amazonaws.com"
        }
      }
    }
  ]
}
EOF

    aws iam put-role-policy \
      --role-name ecsTaskExecutionRole \
      --policy-name SecretsManagerAccess \
      --policy-document file:///tmp/ecs-secrets-policy.json

    echo "✓ Secrets Manager access policy attached"

    # Get role ARN
    ECS_TASK_EXECUTION_ROLE_ARN=$(aws iam get-role --role-name ecsTaskExecutionRole --query 'Role.Arn' --output text)
fi

echo "  Role ARN: ${ECS_TASK_EXECUTION_ROLE_ARN}"

# Create ECS Task Role (for application runtime permissions)
echo ""
echo "Step 3: Creating ECS Task Role..."

if aws iam get-role --role-name ecsTaskRole > /dev/null 2>&1; then
    echo "✓ ecsTaskRole already exists, skipping creation"
    ECS_TASK_ROLE_ARN=$(aws iam get-role --role-name ecsTaskRole --query 'Role.Arn' --output text)
else
    aws iam create-role \
      --role-name ecsTaskRole \
      --assume-role-policy-document file:///tmp/ecs-task-trust-policy.json \
      --description "Allows ECS tasks to access AWS resources" \
      > /dev/null

    echo "✓ ecsTaskRole created"

    # Create policy for application permissions (add as needed)
    cat > /tmp/ecs-task-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "*"
    }
  ]
}
EOF

    aws iam put-role-policy \
      --role-name ecsTaskRole \
      --policy-name ApplicationPermissions \
      --policy-document file:///tmp/ecs-task-policy.json

    echo "✓ Application permissions policy attached"

    # Get role ARN
    ECS_TASK_ROLE_ARN=$(aws iam get-role --role-name ecsTaskRole --query 'Role.Arn' --output text)
fi

echo "  Role ARN: ${ECS_TASK_ROLE_ARN}"

# Clean up temp files
rm -f /tmp/ecs-task-trust-policy.json /tmp/ecs-secrets-policy.json /tmp/ecs-task-policy.json

# Append IAM role ARNs to resources file
echo ""
echo "Step 4: Saving IAM role ARNs..."
cat >> ${RESOURCES_FILE} <<EOF

# IAM Roles
export AWS_ACCOUNT_ID="${AWS_ACCOUNT_ID}"
export ECS_TASK_EXECUTION_ROLE_ARN="${ECS_TASK_EXECUTION_ROLE_ARN}"
export ECS_TASK_ROLE_ARN="${ECS_TASK_ROLE_ARN}"
EOF

echo "✓ IAM role ARNs appended to ${RESOURCES_FILE}"

echo ""
echo "======================================"
echo "✓ IAM Roles Created Successfully!"
echo "======================================"
echo ""
echo "Summary:"
echo "  Task Execution Role: ecsTaskExecutionRole"
echo "  Task Execution Role ARN: ${ECS_TASK_EXECUTION_ROLE_ARN}"
echo "  Task Role: ecsTaskRole"
echo "  Task Role ARN: ${ECS_TASK_ROLE_ARN}"
echo ""
echo "Next step: Run ./04-create-secrets.sh"
