#!/bin/bash

# Create Application Load Balancer for SchoolDay
# This script creates ALB, target group, and listener

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESOURCES_FILE="${SCRIPT_DIR}/aws-resources.env"

if [ ! -f "$RESOURCES_FILE" ]; then
    echo "Error: ${RESOURCES_FILE} not found!"
    exit 1
fi

source "$RESOURCES_FILE"

echo "======================================"
echo "Creating Application Load Balancer"
echo "======================================"

# Create ALB
echo "Step 1: Creating ALB..."
ALB_ARN=$(aws elbv2 create-load-balancer \
  --name schoolday-alb \
  --subnets ${PUBLIC_SUBNET_1} ${PUBLIC_SUBNET_2} \
  --security-groups ${ALB_SG} \
  --scheme internet-facing \
  --type application \
  --region ${AWS_REGION} \
  --query 'LoadBalancers[0].LoadBalancerArn' \
  --output text)

echo "✓ ALB Created: ${ALB_ARN}"

# Create Target Group
echo "Step 2: Creating Target Group..."
TG_ARN=$(aws elbv2 create-target-group \
  --name schoolday-tg \
  --protocol HTTP \
  --port 8081 \
  --vpc-id ${VPC_ID} \
  --target-type ip \
  --health-check-enabled \
  --health-check-protocol HTTP \
  --health-check-path /actuator/health \
  --health-check-interval-seconds 30 \
  --health-check-timeout-seconds 5 \
  --healthy-threshold-count 2 \
  --unhealthy-threshold-count 3 \
  --region ${AWS_REGION} \
  --query 'TargetGroups[0].TargetGroupArn' \
  --output text)

echo "✓ Target Group Created: ${TG_ARN}"

# Create Listener
echo "Step 3: Creating Listener..."
aws elbv2 create-listener \
  --load-balancer-arn ${ALB_ARN} \
  --protocol HTTP \
  --port 80 \
  --default-actions Type=forward,TargetGroupArn=${TG_ARN} \
  --region ${AWS_REGION} \
  > /dev/null

echo "✓ Listener Created"

# Get ALB DNS name
ALB_DNS=$(aws elbv2 describe-load-balancers \
  --load-balancer-arns ${ALB_ARN} \
  --region ${AWS_REGION} \
  --query 'LoadBalancers[0].DNSName' \
  --output text)

# Save to resources file
cat >> ${RESOURCES_FILE} <<EOF

# Application Load Balancer
export ALB_ARN="${ALB_ARN}"
export TG_ARN="${TG_ARN}"
export ALB_DNS="${ALB_DNS}"
EOF

echo ""
echo "======================================"
echo "✓ ALB Created Successfully!"
echo "======================================"
echo "  ALB DNS: ${ALB_DNS}"
echo ""
echo "Next step: Run ./07-create-ecs.sh"
