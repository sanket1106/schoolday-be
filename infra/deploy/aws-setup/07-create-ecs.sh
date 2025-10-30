#!/bin/bash

# Create ECS Cluster and Service for SchoolDay
# This script creates the ECS cluster and deploys the service

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESOURCES_FILE="${SCRIPT_DIR}/aws-resources.env"

if [ ! -f "$RESOURCES_FILE" ]; then
    echo "Error: ${RESOURCES_FILE} not found!"
    exit 1
fi

source "$RESOURCES_FILE"

ECR_IMAGE_URI=${1}
DESIRED_COUNT=${2:-2}

echo "======================================"
echo "Creating ECS Cluster and Service"
echo "======================================"

# Create ECS Cluster
echo "Step 1: Creating ECS cluster..."
aws ecs create-cluster \
  --cluster-name schoolday-cluster \
  --region ${AWS_REGION} \
  > /dev/null

echo "✓ ECS Cluster Created: schoolday-cluster"

# Create CloudWatch Log Group
echo "Step 2: Creating CloudWatch log group..."
aws logs create-log-group \
  --log-group-name /ecs/schoolday-app \
  --region ${AWS_REGION} \
  2>/dev/null || echo "  (Log group may already exist)"

echo "✓ Log group ready"

# Update task definition with actual values
echo "Step 3: Updating task definition..."
TASK_DEF_FILE="${SCRIPT_DIR}/../ecs-task-definition.json"

if [ ! -f "$TASK_DEF_FILE" ]; then
    echo "Error: ${TASK_DEF_FILE} not found!"
    exit 1
fi

# Create temp task definition with substituted values
TEMP_TASK_DEF="/tmp/schoolday-task-def-$(date +%s).json"
cat "$TASK_DEF_FILE" | \
  sed "s|{ACCOUNT_ID}|${AWS_ACCOUNT_ID}|g" | \
  sed "s|{REGION}|${AWS_REGION}|g" | \
  sed "s|{RDS_ENDPOINT}|${RDS_ENDPOINT}|g" | \
  sed "s|schoolday/mysql-password|${MYSQL_PASSWORD_SECRET_ARN}|g" \
  > "$TEMP_TASK_DEF"

if [ -n "$ECR_IMAGE_URI" ]; then
    # Replace image URI if provided
    sed -i '' "s|${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/schoolday-app:latest|${ECR_IMAGE_URI}|g" "$TEMP_TASK_DEF"
fi

# Register task definition
TASK_DEF_ARN=$(aws ecs register-task-definition \
  --cli-input-json file://${TEMP_TASK_DEF} \
  --region ${AWS_REGION} \
  --query 'taskDefinition.taskDefinitionArn' \
  --output text)

rm "$TEMP_TASK_DEF"

echo "✓ Task Definition Registered: ${TASK_DEF_ARN}"

# Create ECS Service
echo "Step 4: Creating ECS service..."
aws ecs create-service \
  --cluster schoolday-cluster \
  --service-name schoolday-service \
  --task-definition ${TASK_DEF_ARN} \
  --desired-count ${DESIRED_COUNT} \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[${PRIVATE_SUBNET_1},${PRIVATE_SUBNET_2}],securityGroups=[${ECS_SG}],assignPublicIp=DISABLED}" \
  --load-balancers "targetGroupArn=${TG_ARN},containerName=schoolday-app,containerPort=8081" \
  --health-check-grace-period-seconds 60 \
  --region ${AWS_REGION} \
  > /dev/null

echo "✓ ECS Service Created: schoolday-service"

# Wait for service to stabilize
echo "Step 5: Waiting for service to stabilize..."
aws ecs wait services-stable \
  --cluster schoolday-cluster \
  --services schoolday-service \
  --region ${AWS_REGION}

echo "✓ Service is stable"

# Save to resources file
cat >> ${RESOURCES_FILE} <<EOF

# ECS Cluster and Service
export ECS_CLUSTER="schoolday-cluster"
export ECS_SERVICE="schoolday-service"
export TASK_DEF_ARN="${TASK_DEF_ARN}"
EOF

echo ""
echo "======================================"
echo "✓ ECS Deployment Completed!"
echo "======================================"
echo "  Cluster: schoolday-cluster"
echo "  Service: schoolday-service"
echo "  ALB URL: http://${ALB_DNS}"
echo ""
echo "Test your deployment:"
echo "  curl http://${ALB_DNS}/actuator/health"
