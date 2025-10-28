# AWS ECS Deployment Guide for SchoolDay Backend

This guide provides step-by-step instructions for deploying the SchoolDay Spring Boot application to AWS ECS (Elastic Container Service) using Fargate.

## Table of Contents
- [Architecture Overview](#architecture-overview)
- [Prerequisites](#prerequisites)
- [Local Testing with Docker](#local-testing-with-docker)
- [AWS Infrastructure Setup](#aws-infrastructure-setup)
- [Database Setup (RDS)](#database-setup-rds)
- [Build and Push Docker Image](#build-and-push-docker-image)
- [Deploy to ECS](#deploy-to-ecs)
- [Monitoring and Troubleshooting](#monitoring-and-troubleshooting)
- [CI/CD Integration](#cicd-integration)
- [Cost Optimization](#cost-optimization)

---

## Architecture Overview

```
┌─────────────┐
│   Internet  │
└──────┬──────┘
       │
┌──────▼──────────────────────────────────────────┐
│  Application Load Balancer (ALB)                │
└──────┬──────────────────────────────────────────┘
       │
┌──────▼──────────────────────────────────────────┐
│  ECS Cluster (Fargate)                          │
│  ┌────────────────────────────────────────┐    │
│  │  ECS Service                            │    │
│  │  ┌──────────────┐  ┌──────────────┐   │    │
│  │  │  Task 1      │  │  Task 2      │   │    │
│  │  │  (Container) │  │  (Container) │   │    │
│  │  └──────┬───────┘  └──────┬───────┘   │    │
│  └─────────┼──────────────────┼───────────┘    │
└────────────┼──────────────────┼────────────────┘
             │                  │
        ┌────▼──────────────────▼────┐
        │  RDS MySQL Database        │
        │  (Multi-AZ for HA)         │
        └────────────────────────────┘
```

**Components:**
- **ECS Fargate**: Serverless container orchestration
- **Application Load Balancer**: Routes traffic to ECS tasks
- **RDS MySQL**: Managed MySQL database
- **ECR**: Container image registry
- **CloudWatch**: Logging and monitoring
- **Secrets Manager**: Secure credential storage

---

## Prerequisites

### Required Tools
1. **AWS CLI** (v2.x)
   ```bash
   aws --version
   # Install: https://aws.amazon.com/cli/
   ```

2. **Docker** (20.x or higher)
   ```bash
   docker --version
   ```

3. **AWS Account** with appropriate permissions:
   - ECS full access
   - ECR full access
   - RDS full access
   - VPC management
   - IAM role creation
   - Secrets Manager access
   - CloudWatch Logs access

### AWS CLI Configuration
```bash
# Configure AWS credentials
aws configure

# Verify configuration
aws sts get-caller-identity
```

---

## Local Testing with Docker

Before deploying to AWS, test the application locally using Docker Compose.

### 1. Set Environment Variables
```bash
# Copy and configure environment file
cp infra/.env.example infra/.env

# Edit infra/.env and set:
export MYSQL_ROOT_PASSWORD=your_secure_password
export MYSQL_DATABASE=schoolday
```

### 2. Build and Run with Docker Compose
```bash
# Build and start all services
cd infra/deploy
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

### 3. Test the Application
```bash
# Health check
curl http://localhost:8081/actuator/health

# Test API endpoints
curl http://localhost:8081/api/users
```

---

## AWS Infrastructure Setup

### Step 1: Create VPC and Networking

#### Option A: Use AWS Console
1. Go to **VPC Console**
2. Click **Create VPC**
3. Select **VPC and more** (creates VPC, subnets, NAT gateway, etc.)
4. Configure:
   - Name: `schoolday-vpc`
   - IPv4 CIDR: `10.0.0.0/16`
   - Availability Zones: 2
   - Public subnets: 2
   - Private subnets: 2
   - NAT gateways: 1 (or 2 for HA)

#### Option B: Use AWS CLI
```bash
# Create VPC
VPC_ID=$(aws ec2 create-vpc \
  --cidr-block 10.0.0.0/16 \
  --tag-specifications 'ResourceType=vpc,Tags=[{Key=Name,Value=schoolday-vpc}]' \
  --query 'Vpc.VpcId' \
  --output text)

echo "VPC ID: $VPC_ID"

# Create Internet Gateway
IGW_ID=$(aws ec2 create-internet-gateway \
  --tag-specifications 'ResourceType=internet-gateway,Tags=[{Key=Name,Value=schoolday-igw}]' \
  --query 'InternetGateway.InternetGatewayId' \
  --output text)

aws ec2 attach-internet-gateway --vpc-id $VPC_ID --internet-gateway-id $IGW_ID

# Create Public Subnets (2 for HA)
PUBLIC_SUBNET_1=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.1.0/24 \
  --availability-zone us-east-1a \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=schoolday-public-subnet-1}]' \
  --query 'Subnet.SubnetId' \
  --output text)

PUBLIC_SUBNET_2=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.2.0/24 \
  --availability-zone us-east-1b \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=schoolday-public-subnet-2}]' \
  --query 'Subnet.SubnetId' \
  --output text)

# Create Private Subnets (2 for HA)
PRIVATE_SUBNET_1=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.11.0/24 \
  --availability-zone us-east-1a \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=schoolday-private-subnet-1}]' \
  --query 'Subnet.SubnetId' \
  --output text)

PRIVATE_SUBNET_2=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.12.0/24 \
  --availability-zone us-east-1b \
  --tag-specifications 'ResourceType=subnet,Tags=[{Key=Name,Value=schoolday-private-subnet-2}]' \
  --query 'Subnet.SubnetId' \
  --output text)

echo "Subnets created:"
echo "Public Subnet 1: $PUBLIC_SUBNET_1"
echo "Public Subnet 2: $PUBLIC_SUBNET_2"
echo "Private Subnet 1: $PRIVATE_SUBNET_1"
echo "Private Subnet 2: $PRIVATE_SUBNET_2"

# Save these IDs - you'll need them later
```

### Step 2: Create Security Groups

```bash
# Security Group for ALB (allows HTTP/HTTPS from internet)
ALB_SG=$(aws ec2 create-security-group \
  --group-name schoolday-alb-sg \
  --description "Security group for SchoolDay ALB" \
  --vpc-id $VPC_ID \
  --query 'GroupId' \
  --output text)

# Allow HTTP traffic
aws ec2 authorize-security-group-ingress \
  --group-id $ALB_SG \
  --protocol tcp \
  --port 80 \
  --cidr 0.0.0.0/0

# Allow HTTPS traffic
aws ec2 authorize-security-group-ingress \
  --group-id $ALB_SG \
  --protocol tcp \
  --port 443 \
  --cidr 0.0.0.0/0

# Security Group for ECS Tasks (allows traffic from ALB)
ECS_SG=$(aws ec2 create-security-group \
  --group-name schoolday-ecs-sg \
  --description "Security group for SchoolDay ECS tasks" \
  --vpc-id $VPC_ID \
  --query 'GroupId' \
  --output text)

# Allow traffic from ALB on port 8081
aws ec2 authorize-security-group-ingress \
  --group-id $ECS_SG \
  --protocol tcp \
  --port 8081 \
  --source-group $ALB_SG

# Security Group for RDS (allows traffic from ECS)
RDS_SG=$(aws ec2 create-security-group \
  --group-name schoolday-rds-sg \
  --description "Security group for SchoolDay RDS" \
  --vpc-id $VPC_ID \
  --query 'GroupId' \
  --output text)

# Allow MySQL traffic from ECS
aws ec2 authorize-security-group-ingress \
  --group-id $RDS_SG \
  --protocol tcp \
  --port 3306 \
  --source-group $ECS_SG

echo "Security Groups created:"
echo "ALB SG: $ALB_SG"
echo "ECS SG: $ECS_SG"
echo "RDS SG: $RDS_SG"
```

### Step 3: Create IAM Roles

```bash
# Create ECS Task Execution Role (required for Fargate)
cat > ecs-task-execution-role-trust-policy.json <<EOF
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

aws iam create-role \
  --role-name ecsTaskExecutionRole \
  --assume-role-policy-document file://ecs-task-execution-role-trust-policy.json

# Attach managed policy
aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

# Add Secrets Manager access
cat > ecs-secrets-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": "arn:aws:secretsmanager:*:*:secret:schoolday/*"
    }
  ]
}
EOF

aws iam put-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-name SecretsManagerAccess \
  --policy-document file://ecs-secrets-policy.json

# Create ECS Task Role (for application runtime permissions)
aws iam create-role \
  --role-name ecsTaskRole \
  --assume-role-policy-document file://ecs-task-execution-role-trust-policy.json

echo "IAM Roles created successfully"
```

### Step 4: Store Secrets in AWS Secrets Manager

```bash
# Store MySQL password
aws secretsmanager create-secret \
  --name schoolday/mysql-password \
  --description "MySQL root password for SchoolDay database" \
  --secret-string "your_secure_password_here"

echo "Secret stored successfully"
```

---

## Database Setup (RDS)

### Step 1: Create RDS Subnet Group

```bash
# Create DB subnet group (use private subnets)
aws rds create-db-subnet-group \
  --db-subnet-group-name schoolday-db-subnet-group \
  --db-subnet-group-description "Subnet group for SchoolDay RDS" \
  --subnet-ids $PRIVATE_SUBNET_1 $PRIVATE_SUBNET_2
```

### Step 2: Create RDS MySQL Instance

```bash
# Create RDS instance
aws rds create-db-instance \
  --db-instance-identifier schoolday-db \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version 8.0.35 \
  --master-username root \
  --master-user-password "your_secure_password_here" \
  --allocated-storage 20 \
  --storage-type gp3 \
  --vpc-security-group-ids $RDS_SG \
  --db-subnet-group-name schoolday-db-subnet-group \
  --backup-retention-period 7 \
  --preferred-backup-window "03:00-04:00" \
  --preferred-maintenance-window "mon:04:00-mon:05:00" \
  --multi-az \
  --publicly-accessible false \
  --storage-encrypted \
  --enable-cloudwatch-logs-exports '["error","general","slowquery"]' \
  --tags Key=Name,Value=schoolday-db

# Wait for instance to be available (takes 5-10 minutes)
echo "Waiting for RDS instance to become available..."
aws rds wait db-instance-available --db-instance-identifier schoolday-db

# Get RDS endpoint
RDS_ENDPOINT=$(aws rds describe-db-instances \
  --db-instance-identifier schoolday-db \
  --query 'DBInstances[0].Endpoint.Address' \
  --output text)

echo "RDS Endpoint: $RDS_ENDPOINT"
```

### Step 3: Initialize Database Schema

```bash
# Connect to RDS from bastion host or local machine with VPN
mysql -h $RDS_ENDPOINT -u root -p

# Create database and apply schema
mysql -h $RDS_ENDPOINT -u root -p < infra/db/sql/schema.sql
mysql -h $RDS_ENDPOINT -u root -p < infra/db/sql/basedata.sql
```

**Alternative: Use AWS Systems Manager Session Manager**
```bash
# Create a temporary EC2 instance in the same VPC to run schema scripts
# Or use AWS Lambda function with VPC access
```

---

## Build and Push Docker Image

### Step 1: Get AWS Account ID
```bash
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
AWS_REGION="us-east-1"

echo "AWS Account ID: $AWS_ACCOUNT_ID"
echo "AWS Region: $AWS_REGION"
```

### Step 2: Build and Push Using Script

```bash
# Navigate to project root
cd /path/to/SchoolDay

# Run build and push script
./infra/deploy/build-and-push.sh $AWS_ACCOUNT_ID $AWS_REGION latest

# Or build for specific version
./infra/deploy/build-and-push.sh $AWS_ACCOUNT_ID $AWS_REGION v1.0.0
```

### Step 3: Verify Image in ECR

```bash
# List images in repository
aws ecr describe-images \
  --repository-name schoolday-app \
  --region $AWS_REGION
```

---

## Deploy to ECS

### Step 1: Create Application Load Balancer

```bash
# Create ALB
ALB_ARN=$(aws elbv2 create-load-balancer \
  --name schoolday-alb \
  --subnets $PUBLIC_SUBNET_1 $PUBLIC_SUBNET_2 \
  --security-groups $ALB_SG \
  --scheme internet-facing \
  --type application \
  --query 'LoadBalancers[0].LoadBalancerArn' \
  --output text)

# Create Target Group
TG_ARN=$(aws elbv2 create-target-group \
  --name schoolday-tg \
  --protocol HTTP \
  --port 8081 \
  --vpc-id $VPC_ID \
  --target-type ip \
  --health-check-enabled \
  --health-check-protocol HTTP \
  --health-check-path /actuator/health \
  --health-check-interval-seconds 30 \
  --health-check-timeout-seconds 5 \
  --healthy-threshold-count 2 \
  --unhealthy-threshold-count 3 \
  --query 'TargetGroups[0].TargetGroupArn' \
  --output text)

# Create Listener
aws elbv2 create-listener \
  --load-balancer-arn $ALB_ARN \
  --protocol HTTP \
  --port 80 \
  --default-actions Type=forward,TargetGroupArn=$TG_ARN

# Get ALB DNS name
ALB_DNS=$(aws elbv2 describe-load-balancers \
  --load-balancer-arns $ALB_ARN \
  --query 'LoadBalancers[0].DNSName' \
  --output text)

echo "ALB DNS: $ALB_DNS"
```

### Step 2: Create CloudWatch Log Group

```bash
aws logs create-log-group --log-group-name /ecs/schoolday-app
```

### Step 3: Update ECS Task Definition

Edit `infra/deploy/ecs-task-definition.json` and replace placeholders:
- `{ACCOUNT_ID}` → Your AWS Account ID
- `{REGION}` → Your AWS Region (e.g., us-east-1)
- `{RDS_ENDPOINT}` → Your RDS endpoint

```bash
# Use sed to replace placeholders
sed -i "s/{ACCOUNT_ID}/$AWS_ACCOUNT_ID/g" infra/deploy/ecs-task-definition.json
sed -i "s/{REGION}/$AWS_REGION/g" infra/deploy/ecs-task-definition.json
sed -i "s/{RDS_ENDPOINT}/$RDS_ENDPOINT/g" infra/deploy/ecs-task-definition.json
```

### Step 4: Create ECS Cluster

```bash
aws ecs create-cluster --cluster-name schoolday-cluster
```

### Step 5: Create ECS Service

```bash
# Register task definition
TASK_DEF_ARN=$(aws ecs register-task-definition \
  --cli-input-json file://infra/deploy/ecs-task-definition.json \
  --query 'taskDefinition.taskDefinitionArn' \
  --output text)

echo "Task Definition: $TASK_DEF_ARN"

# Create ECS service
aws ecs create-service \
  --cluster schoolday-cluster \
  --service-name schoolday-service \
  --task-definition $TASK_DEF_ARN \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[$PRIVATE_SUBNET_1,$PRIVATE_SUBNET_2],securityGroups=[$ECS_SG],assignPublicIp=DISABLED}" \
  --load-balancers "targetGroupArn=$TG_ARN,containerName=schoolday-app,containerPort=8081" \
  --health-check-grace-period-seconds 60

echo "ECS Service created successfully!"
```

### Step 6: Wait for Deployment

```bash
# Wait for service to stabilize
aws ecs wait services-stable \
  --cluster schoolday-cluster \
  --services schoolday-service

echo "Service is stable and running!"
```

### Step 7: Test the Deployment

```bash
# Test health endpoint
curl http://$ALB_DNS/actuator/health

# Test API
curl http://$ALB_DNS/api/users
```

---

## Monitoring and Troubleshooting

### View ECS Service Status
```bash
aws ecs describe-services \
  --cluster schoolday-cluster \
  --services schoolday-service
```

### View Running Tasks
```bash
aws ecs list-tasks \
  --cluster schoolday-cluster \
  --service-name schoolday-service
```

### View Container Logs
```bash
# Tail logs
aws logs tail /ecs/schoolday-app --follow

# Get logs for specific time range
aws logs tail /ecs/schoolday-app \
  --since 1h \
  --format short
```

### Check Task Health
```bash
# Get task ARN
TASK_ARN=$(aws ecs list-tasks \
  --cluster schoolday-cluster \
  --service-name schoolday-service \
  --query 'taskArns[0]' \
  --output text)

# Describe task
aws ecs describe-tasks \
  --cluster schoolday-cluster \
  --tasks $TASK_ARN
```

### Common Issues

#### 1. Tasks Keep Restarting
- Check logs: `aws logs tail /ecs/schoolday-app --follow`
- Verify environment variables in task definition
- Check RDS connectivity from ECS security group
- Verify Secrets Manager permissions

#### 2. Health Checks Failing
- Ensure `/actuator/health` endpoint is accessible
- Check health check settings in target group
- Verify application is listening on port 8081
- Increase health check grace period

#### 3. Database Connection Issues
- Verify RDS security group allows traffic from ECS security group
- Check RDS endpoint in task definition
- Verify MySQL password in Secrets Manager
- Ensure RDS instance is in available state

#### 4. Image Pull Errors
- Verify ECR repository exists
- Check task execution role has ECR permissions
- Ensure image tag exists in ECR

---

## CI/CD Integration

### GitHub Actions Example

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to AWS ECS

on:
  push:
    branches: [main, master]

env:
  AWS_REGION: us-east-1
  ECR_REPOSITORY: schoolday-app
  ECS_CLUSTER: schoolday-cluster
  ECS_SERVICE: schoolday-service

jobs:
  deploy:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v2
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: ${{ env.AWS_REGION }}

      - name: Login to Amazon ECR
        id: login-ecr
        uses: aws-actions/amazon-ecr-login@v1

      - name: Build and push Docker image
        env:
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          IMAGE_TAG: ${{ github.sha }}
        run: |
          docker build -f infra/deploy/Dockerfile -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
          docker tag $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG $ECR_REGISTRY/$ECR_REPOSITORY:latest
          docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest

      - name: Update ECS service
        run: |
          aws ecs update-service \
            --cluster $ECS_CLUSTER \
            --service $ECS_SERVICE \
            --force-new-deployment
```

---

## Cost Optimization

### Estimated Monthly Costs

| Service | Configuration | Estimated Cost |
|---------|--------------|----------------|
| **ECS Fargate** | 2 tasks × 0.5 vCPU, 1 GB RAM | ~$30-40 |
| **RDS MySQL** | db.t3.micro, Multi-AZ | ~$40-50 |
| **Application Load Balancer** | 1 ALB | ~$20-25 |
| **NAT Gateway** | 1 NAT Gateway | ~$35-45 |
| **CloudWatch Logs** | Standard logging | ~$5-10 |
| **Data Transfer** | Varies by traffic | ~$10-20 |
| **Total** | | **~$140-190/month** |

### Cost Reduction Tips

1. **Use Single AZ for Development**
   ```bash
   # Single-AZ RDS (saves ~50%)
   --no-multi-az
   ```

2. **Reduce Task Count**
   ```bash
   # 1 task for dev/staging
   --desired-count 1
   ```

3. **Use Spot Instances** (Fargate Spot)
   ```bash
   # Add capacity provider strategy
   --capacity-provider-strategy \
     capacityProvider=FARGATE_SPOT,weight=1
   ```

4. **Stop Non-Production Environments**
   ```bash
   # Stop ECS service during off-hours
   aws ecs update-service \
     --cluster schoolday-cluster \
     --service schoolday-service \
     --desired-count 0

   # Stop RDS instance
   aws rds stop-db-instance \
     --db-instance-identifier schoolday-db
   ```

5. **Use Reserved Instances** for production
   - RDS Reserved Instances: ~40% savings
   - Savings Plans for Fargate: ~20% savings

---

## Updating the Application

### Deploy New Version

```bash
# 1. Build and push new image
./infra/deploy/build-and-push.sh $AWS_ACCOUNT_ID $AWS_REGION v1.0.1

# 2. Update task definition (if needed)
# Edit infra/deploy/ecs-task-definition.json

# 3. Deploy to ECS
./infra/deploy/deploy-to-ecs.sh schoolday-cluster schoolday-service us-east-1
```

### Rollback to Previous Version

```bash
# List task definition revisions
aws ecs list-task-definitions --family-prefix schoolday-app

# Update service to previous revision
aws ecs update-service \
  --cluster schoolday-cluster \
  --service schoolday-service \
  --task-definition schoolday-app:REVISION_NUMBER
```

---

## Cleanup (Destroy Infrastructure)

**Warning:** This will delete all resources and data.

```bash
# Delete ECS service
aws ecs delete-service \
  --cluster schoolday-cluster \
  --service schoolday-service \
  --force

# Delete ECS cluster
aws ecs delete-cluster --cluster schoolday-cluster

# Delete ALB
aws elbv2 delete-load-balancer --load-balancer-arn $ALB_ARN
aws elbv2 delete-target-group --target-group-arn $TG_ARN

# Delete RDS instance
aws rds delete-db-instance \
  --db-instance-identifier schoolday-db \
  --skip-final-snapshot

# Delete ECR repository
aws ecr delete-repository \
  --repository-name schoolday-app \
  --force

# Delete secrets
aws secretsmanager delete-secret \
  --secret-id schoolday/mysql-password \
  --force-delete-without-recovery

# Delete security groups, subnets, VPC
# (Reverse order of creation)
```

---

## Next Steps

1. **Set up Custom Domain**
   - Register domain in Route 53
   - Create SSL certificate in ACM
   - Configure HTTPS listener on ALB

2. **Enable Auto Scaling**
   - Configure ECS service auto-scaling based on CPU/memory
   - Configure RDS read replicas for read-heavy workloads

3. **Set up Monitoring**
   - Create CloudWatch dashboards
   - Set up CloudWatch alarms for critical metrics
   - Configure SNS for alerts

4. **Implement Blue/Green Deployment**
   - Use AWS CodeDeploy for zero-downtime deployments

5. **Add WAF Protection**
   - Attach AWS WAF to ALB for additional security

---

## Additional Resources

- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [AWS Fargate Pricing](https://aws.amazon.com/fargate/pricing/)
- [RDS Best Practices](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_BestPractices.html)
- [ECS Task Definition Parameters](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task_definition_parameters.html)

---

**Need Help?** Check the [Troubleshooting](#monitoring-and-troubleshooting) section or review AWS CloudWatch logs for detailed error messages.
