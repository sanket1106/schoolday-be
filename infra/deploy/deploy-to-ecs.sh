#!/bin/bash

# Deploy application to AWS ECS
# Usage: ./deploy-to-ecs.sh <cluster-name> <service-name> <region>

set -e

CLUSTER_NAME=${1:-schoolday-cluster}
SERVICE_NAME=${2:-schoolday-service}
AWS_REGION=${3:-us-east-1}

echo "======================================"
echo "Deploying to AWS ECS"
echo "======================================"
echo "Cluster: ${CLUSTER_NAME}"
echo "Service: ${SERVICE_NAME}"
echo "Region: ${AWS_REGION}"
echo "======================================"

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo ""
echo "Step 1: Register new task definition"
TASK_DEFINITION_ARN=$(aws ecs register-task-definition \
    --cli-input-json file://${SCRIPT_DIR}/ecs-task-definition.json \
    --region ${AWS_REGION} \
    --query 'taskDefinition.taskDefinitionArn' \
    --output text)

echo "Task Definition ARN: ${TASK_DEFINITION_ARN}"

echo ""
echo "Step 2: Update ECS service"
aws ecs update-service \
    --cluster ${CLUSTER_NAME} \
    --service ${SERVICE_NAME} \
    --task-definition ${TASK_DEFINITION_ARN} \
    --force-new-deployment \
    --region ${AWS_REGION}

echo ""
echo "Step 3: Wait for service to stabilize"
echo "This may take a few minutes..."
aws ecs wait services-stable \
    --cluster ${CLUSTER_NAME} \
    --services ${SERVICE_NAME} \
    --region ${AWS_REGION}

echo ""
echo "======================================"
echo "✓ Deployment completed successfully!"
echo "======================================"
echo ""
echo "Check service status:"
echo "  aws ecs describe-services --cluster ${CLUSTER_NAME} --services ${SERVICE_NAME} --region ${AWS_REGION}"
echo ""
echo "View logs:"
echo "  aws logs tail /ecs/schoolday-app --follow --region ${AWS_REGION}"
