#!/bin/bash

# Build and push Docker image to AWS ECR
# Usage: ./build-and-push.sh <aws-account-id> <region> <image-tag>

set -e

AWS_ACCOUNT_ID=$1
AWS_REGION=${2:-us-east-1}
IMAGE_TAG=${3:-latest}

if [ -z "$AWS_ACCOUNT_ID" ]; then
    echo "Error: AWS Account ID is required"
    echo "Usage: $0 <aws-account-id> <region> <image-tag>"
    exit 1
fi

ECR_REPOSITORY="schoolday-app"
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
IMAGE_URI="${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}"

echo "======================================"
echo "Building and Pushing Docker Image"
echo "======================================"
echo "AWS Account: ${AWS_ACCOUNT_ID}"
echo "Region: ${AWS_REGION}"
echo "Repository: ${ECR_REPOSITORY}"
echo "Image Tag: ${IMAGE_TAG}"
echo "======================================"

# Navigate to project root (2 levels up from this script)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
cd "${PROJECT_ROOT}"

echo "Step 1: Authenticate Docker to ECR"
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}

echo ""
echo "Step 2: Check if ECR repository exists"
if ! aws ecr describe-repositories --repository-names ${ECR_REPOSITORY} --region ${AWS_REGION} > /dev/null 2>&1; then
    echo "Repository does not exist. Creating..."
    aws ecr create-repository \
        --repository-name ${ECR_REPOSITORY} \
        --region ${AWS_REGION} \
        --image-scanning-configuration scanOnPush=true \
        --encryption-configuration encryptionType=AES256
    echo "Repository created successfully!"
else
    echo "Repository already exists."
fi

echo ""
echo "Step 3: Build Docker image"
docker build \
    -f infra/deploy/Dockerfile \
    -t ${ECR_REPOSITORY}:${IMAGE_TAG} \
    -t ${IMAGE_URI} \
    .

echo ""
echo "Step 4: Push image to ECR"
docker push ${IMAGE_URI}

echo ""
echo "======================================"
echo "✓ Build and push completed successfully!"
echo "======================================"
echo "Image URI: ${IMAGE_URI}"
echo ""
echo "Next steps:"
echo "1. Update ECS task definition with this image URI"
echo "2. Deploy to ECS using './deploy-to-ecs.sh'"
