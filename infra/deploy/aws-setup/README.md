# AWS Setup Scripts for SchoolDay ECS Deployment

Automated scripts to set up complete AWS infrastructure for deploying SchoolDay backend to ECS Fargate.

## Overview

These scripts automate the manual steps described in [AWS_ECS_DEPLOYMENT.md](../../../AWS_ECS_DEPLOYMENT.md).

## Prerequisites

1. **AWS CLI** installed and configured
   ```bash
   aws --version
   aws configure
   ```

2. **Appropriate AWS permissions** for:
   - VPC, EC2, and networking
   - IAM roles
   - ECS and ECR
   - RDS
   - Secrets Manager
   - CloudWatch Logs
   - Application Load Balancer

3. **Docker** installed (for building and pushing images)

## Quick Start

### Option 1: Run All Scripts at Once (Recommended)

```bash
cd infra/deploy/aws-setup

# Run master setup script
./setup-all.sh us-east-1
```

This will:
1. Create VPC and networking
2. Create security groups
3. Create IAM roles
4. Store MySQL password in Secrets Manager
5. Create RDS database
6. Create Application Load Balancer
7. Create ECS cluster and service

**Total time:** ~15-20 minutes

### Option 2: Run Scripts Individually

```bash
cd infra/deploy/aws-setup

# 1. Create VPC (2-3 minutes)
./01-create-vpc.sh us-east-1

# Source the resources file to use created IDs
source aws-resources.env

# 2. Create Security Groups (30 seconds)
./02-create-security-groups.sh

# 3. Create IAM Roles (1 minute)
./03-create-iam-roles.sh

# 4. Create Secrets (30 seconds)
./04-create-secrets.sh

# 5. Create RDS Database (5-10 minutes)
./05-create-rds.sh db.t3.micro true

# 6. Create ALB (1 minute)
./06-create-alb.sh

# 7. Create ECS Cluster and Service (2-3 minutes)
./07-create-ecs.sh
```

## Scripts Description

### 01-create-vpc.sh

**Creates:** VPC, subnets, internet gateway, NAT gateway, route tables

**Usage:**
```bash
./01-create-vpc.sh [region] [vpc-name]

# Examples:
./01-create-vpc.sh us-east-1
./01-create-vpc.sh us-west-2 my-schoolday-vpc
```

**Resources Created:**
- VPC (10.0.0.0/16)
- 2 Public subnets (10.0.1.0/24, 10.0.2.0/24)
- 2 Private subnets (10.0.11.0/24, 10.0.12.0/24)
- Internet Gateway
- NAT Gateway (in public subnet 1)
- Public and private route tables

**Output:** Saves all resource IDs to `aws-resources.env`

---

### 02-create-security-groups.sh

**Creates:** Security groups for ALB, ECS, and RDS

**Usage:**
```bash
./02-create-security-groups.sh
```

**Resources Created:**
- ALB Security Group (allows HTTP/HTTPS from internet)
- ECS Security Group (allows traffic from ALB on port 8081)
- RDS Security Group (allows MySQL from ECS on port 3306)

**Requirements:** Run `01-create-vpc.sh` first

---

### 03-create-iam-roles.sh

**Creates:** IAM roles for ECS task execution and runtime

**Usage:**
```bash
./03-create-iam-roles.sh
```

**Resources Created:**
- ecsTaskExecutionRole (for pulling images, logs, secrets)
- ecsTaskRole (for application runtime permissions)

**Note:** Roles are created with proper policies for Secrets Manager access

---

### 04-create-secrets.sh

**Creates:** MySQL password in AWS Secrets Manager

**Usage:**
```bash
./04-create-secrets.sh
```

**Interactive:** Prompts for MySQL password (hidden input)

**Resources Created:**
- Secret: `schoolday/mysql-password`

**Security:** Password is never logged or stored in plain text

---

### 05-create-rds.sh

**Creates:** RDS MySQL database instance

**Usage:**
```bash
./05-create-rds.sh [instance-class] [multi-az]

# Examples:
./05-create-rds.sh db.t3.micro true    # Default (Multi-AZ)
./05-create-rds.sh db.t3.small false   # Single-AZ dev
./05-create-rds.sh db.t3.medium true   # Production
```

**Resources Created:**
- RDS MySQL 8.0.35 instance
- DB subnet group
- Automated backups (7-day retention)
- CloudWatch logs enabled

**Time:** 5-10 minutes (waits for RDS to be available)

---

### 06-create-alb.sh

**Creates:** Application Load Balancer with target group

**Usage:**
```bash
./06-create-alb.sh
```

**Resources Created:**
- Application Load Balancer (internet-facing)
- Target Group (port 8081, IP target type)
- HTTP Listener (port 80)
- Health check configuration

---

### 07-create-ecs.sh

**Creates:** ECS cluster and service

**Usage:**
```bash
./07-create-ecs.sh [image-uri] [desired-count]

# Examples:
./07-create-ecs.sh                     # Use default image URI, 2 tasks
./07-create-ecs.sh <image-uri> 1       # Specify image, 1 task
```

**Resources Created:**
- ECS Cluster: `schoolday-cluster`
- ECS Service: `schoolday-service`
- CloudWatch Log Group: `/ecs/schoolday-app`
- Task definition with environment variables

**Note:** Requires Docker image in ECR. Run `build-and-push.sh` first.

---

### setup-all.sh

**Master script** that runs all scripts in sequence

**Usage:**
```bash
./setup-all.sh [region]

# Example:
./setup-all.sh us-east-1
```

**Interactive:** Prompts for:
- Confirmation to proceed
- MySQL password
- ECR image readiness

**Time:** ~15-20 minutes total

---

## Resource IDs File

All scripts save resource IDs to `aws-resources.env`:

```bash
# Source to use in other scripts
source aws-resources.env

# View contents
cat aws-resources.env

# Example contents:
export VPC_ID="vpc-123456789"
export PUBLIC_SUBNET_1="subnet-123456"
export ALB_SG="sg-123456"
export RDS_ENDPOINT="schoolday-db.abc123.us-east-1.rds.amazonaws.com"
# ... etc
```

## Cost Estimates

Resources created by these scripts:

| Resource | Configuration | Monthly Cost (approx) |
|----------|--------------|----------------------|
| NAT Gateway | 1 gateway | $35-45 |
| RDS MySQL | db.t3.micro Multi-AZ | $40-50 |
| ALB | 1 load balancer | $20-25 |
| ECS Fargate | 2 tasks (0.5 vCPU, 1GB) | $30-40 |
| CloudWatch Logs | Standard | $5-10 |
| **Total** | | **~$130-170/month** |

### Cost Optimization

**Development:**
```bash
# Single-AZ RDS
./05-create-rds.sh db.t3.micro false

# 1 ECS task
./07-create-ecs.sh <image-uri> 1

# Saves ~$40-50/month
```

**Stop during off-hours:**
```bash
# Stop RDS
aws rds stop-db-instance --db-instance-identifier schoolday-db

# Scale ECS to 0
aws ecs update-service --cluster schoolday-cluster --service schoolday-service --desired-count 0
```

## Troubleshooting

### Script Fails: "VPC ID not found"
**Solution:** Run scripts in order, or source `aws-resources.env`

### Script Fails: "Already exists"
**Solution:** Scripts handle existing resources gracefully. Check error message.

### RDS Takes Too Long
**Normal:** RDS creation takes 5-10 minutes. Script waits automatically.

### ECS Service Not Starting
**Check:**
1. Docker image exists in ECR
2. Secrets Manager has correct password
3. RDS is in "available" state
4. Security groups allow traffic

**View logs:**
```bash
aws logs tail /ecs/schoolday-app --follow
```

### Cannot Connect to Database
**Check:**
1. RDS security group allows traffic from ECS security group
2. RDS is in private subnets
3. ECS tasks are in private subnets
4. Password in Secrets Manager matches RDS password

## Cleanup

To delete all resources:

```bash
# Delete in reverse order
aws ecs delete-service --cluster schoolday-cluster --service schoolday-service --force
aws ecs delete-cluster --cluster schoolday-cluster
aws elbv2 delete-load-balancer --load-balancer-arn <ALB_ARN>
aws elbv2 delete-target-group --target-group-arn <TG_ARN>
aws rds delete-db-instance --db-instance-identifier schoolday-db --skip-final-snapshot
aws secretsmanager delete-secret --secret-id schoolday/mysql-password --force-delete-without-recovery

# Delete NAT Gateway, EIP, route tables, subnets, IGW, VPC
# (Follow AWS console or use terraform destroy)
```

## Next Steps

After running these scripts:

1. **Build and push Docker image**
   ```bash
   cd ../
   ./build-and-push.sh <account-id> <region> latest
   ```

2. **Initialize database schema**
   - Connect to RDS from bastion host or VPN
   - Run schema scripts from `infra/db/sql/`

3. **Test deployment**
   ```bash
   curl http://<ALB_DNS>/actuator/health
   ```

4. **Set up CI/CD**
   - See [AWS_ECS_DEPLOYMENT.md](../../../AWS_ECS_DEPLOYMENT.md) for GitHub Actions example

## Support

For issues or questions:
- Check [AWS_ECS_DEPLOYMENT.md](../../../AWS_ECS_DEPLOYMENT.md) for detailed documentation
- View AWS CloudWatch Logs for ECS tasks
- Check AWS Console for resource status

---

**Generated for SchoolDay Backend**
