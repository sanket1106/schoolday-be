#!/bin/bash

# Create VPC and Networking Infrastructure for SchoolDay
# This script creates VPC, subnets, internet gateway, and routing

set -e

AWS_REGION=${1:-us-east-1}
VPC_NAME=${2:-schoolday-vpc}
OUTPUT_FILE="aws-resources.env"

echo "======================================"
echo "Creating VPC and Networking"
echo "======================================"
echo "Region: ${AWS_REGION}"
echo "VPC Name: ${VPC_NAME}"
echo "======================================"

# Create VPC
echo ""
echo "Step 1: Creating VPC..."
VPC_ID=$(aws ec2 create-vpc \
  --cidr-block 10.0.0.0/16 \
  --tag-specifications "ResourceType=vpc,Tags=[{Key=Name,Value=${VPC_NAME}}]" \
  --region ${AWS_REGION} \
  --query 'Vpc.VpcId' \
  --output text)

echo "✓ VPC Created: ${VPC_ID}"

# Enable DNS hostnames
aws ec2 modify-vpc-attribute \
  --vpc-id ${VPC_ID} \
  --enable-dns-hostnames \
  --region ${AWS_REGION}

echo "✓ DNS hostnames enabled"

# Create Internet Gateway
echo ""
echo "Step 2: Creating Internet Gateway..."
IGW_ID=$(aws ec2 create-internet-gateway \
  --tag-specifications "ResourceType=internet-gateway,Tags=[{Key=Name,Value=${VPC_NAME}-igw}]" \
  --region ${AWS_REGION} \
  --query 'InternetGateway.InternetGatewayId' \
  --output text)

echo "✓ Internet Gateway Created: ${IGW_ID}"

# Attach Internet Gateway to VPC
aws ec2 attach-internet-gateway \
  --vpc-id ${VPC_ID} \
  --internet-gateway-id ${IGW_ID} \
  --region ${AWS_REGION}

echo "✓ Internet Gateway attached to VPC"

# Create Public Subnets
echo ""
echo "Step 3: Creating Public Subnets..."
PUBLIC_SUBNET_1=$(aws ec2 create-subnet \
  --vpc-id ${VPC_ID} \
  --cidr-block 10.0.1.0/24 \
  --availability-zone ${AWS_REGION}a \
  --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=${VPC_NAME}-public-subnet-1}]" \
  --region ${AWS_REGION} \
  --query 'Subnet.SubnetId' \
  --output text)

PUBLIC_SUBNET_2=$(aws ec2 create-subnet \
  --vpc-id ${VPC_ID} \
  --cidr-block 10.0.2.0/24 \
  --availability-zone ${AWS_REGION}b \
  --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=${VPC_NAME}-public-subnet-2}]" \
  --region ${AWS_REGION} \
  --query 'Subnet.SubnetId' \
  --output text)

echo "✓ Public Subnet 1 Created: ${PUBLIC_SUBNET_1}"
echo "✓ Public Subnet 2 Created: ${PUBLIC_SUBNET_2}"

# Enable auto-assign public IP for public subnets
aws ec2 modify-subnet-attribute \
  --subnet-id ${PUBLIC_SUBNET_1} \
  --map-public-ip-on-launch \
  --region ${AWS_REGION}

aws ec2 modify-subnet-attribute \
  --subnet-id ${PUBLIC_SUBNET_2} \
  --map-public-ip-on-launch \
  --region ${AWS_REGION}

echo "✓ Auto-assign public IP enabled for public subnets"

# Create Private Subnets
echo ""
echo "Step 4: Creating Private Subnets..."
PRIVATE_SUBNET_1=$(aws ec2 create-subnet \
  --vpc-id ${VPC_ID} \
  --cidr-block 10.0.11.0/24 \
  --availability-zone ${AWS_REGION}a \
  --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=${VPC_NAME}-private-subnet-1}]" \
  --region ${AWS_REGION} \
  --query 'Subnet.SubnetId' \
  --output text)

PRIVATE_SUBNET_2=$(aws ec2 create-subnet \
  --vpc-id ${VPC_ID} \
  --cidr-block 10.0.12.0/24 \
  --availability-zone ${AWS_REGION}b \
  --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=${VPC_NAME}-private-subnet-2}]" \
  --region ${AWS_REGION} \
  --query 'Subnet.SubnetId' \
  --output text)

echo "✓ Private Subnet 1 Created: ${PRIVATE_SUBNET_1}"
echo "✓ Private Subnet 2 Created: ${PRIVATE_SUBNET_2}"

# Create Public Route Table
echo ""
echo "Step 5: Creating Route Tables..."
PUBLIC_RT=$(aws ec2 create-route-table \
  --vpc-id ${VPC_ID} \
  --tag-specifications "ResourceType=route-table,Tags=[{Key=Name,Value=${VPC_NAME}-public-rt}]" \
  --region ${AWS_REGION} \
  --query 'RouteTable.RouteTableId' \
  --output text)

echo "✓ Public Route Table Created: ${PUBLIC_RT}"

# Create route to Internet Gateway
aws ec2 create-route \
  --route-table-id ${PUBLIC_RT} \
  --destination-cidr-block 0.0.0.0/0 \
  --gateway-id ${IGW_ID} \
  --region ${AWS_REGION}

echo "✓ Route to Internet Gateway added"

# Associate public subnets with public route table
aws ec2 associate-route-table \
  --route-table-id ${PUBLIC_RT} \
  --subnet-id ${PUBLIC_SUBNET_1} \
  --region ${AWS_REGION}

aws ec2 associate-route-table \
  --route-table-id ${PUBLIC_RT} \
  --subnet-id ${PUBLIC_SUBNET_2} \
  --region ${AWS_REGION}

echo "✓ Public subnets associated with public route table"

# Allocate Elastic IP for NAT Gateway
echo ""
echo "Step 6: Creating NAT Gateway..."
EIP_ALLOC_ID=$(aws ec2 allocate-address \
  --domain vpc \
  --tag-specifications "ResourceType=elastic-ip,Tags=[{Key=Name,Value=${VPC_NAME}-nat-eip}]" \
  --region ${AWS_REGION} \
  --query 'AllocationId' \
  --output text)

echo "✓ Elastic IP Allocated: ${EIP_ALLOC_ID}"

# Create NAT Gateway
NAT_GW=$(aws ec2 create-nat-gateway \
  --subnet-id ${PUBLIC_SUBNET_1} \
  --allocation-id ${EIP_ALLOC_ID} \
  --tag-specifications "ResourceType=natgateway,Tags=[{Key=Name,Value=${VPC_NAME}-nat-gw}]" \
  --region ${AWS_REGION} \
  --query 'NatGateway.NatGatewayId' \
  --output text)

echo "✓ NAT Gateway Created: ${NAT_GW}"
echo "  Waiting for NAT Gateway to become available..."

# Wait for NAT Gateway to be available
aws ec2 wait nat-gateway-available \
  --nat-gateway-ids ${NAT_GW} \
  --region ${AWS_REGION}

echo "✓ NAT Gateway is now available"

# Create Private Route Table
PRIVATE_RT=$(aws ec2 create-route-table \
  --vpc-id ${VPC_ID} \
  --tag-specifications "ResourceType=route-table,Tags=[{Key=Name,Value=${VPC_NAME}-private-rt}]" \
  --region ${AWS_REGION} \
  --query 'RouteTable.RouteTableId' \
  --output text)

echo "✓ Private Route Table Created: ${PRIVATE_RT}"

# Create route to NAT Gateway
aws ec2 create-route \
  --route-table-id ${PRIVATE_RT} \
  --destination-cidr-block 0.0.0.0/0 \
  --nat-gateway-id ${NAT_GW} \
  --region ${AWS_REGION}

echo "✓ Route to NAT Gateway added"

# Associate private subnets with private route table
aws ec2 associate-route-table \
  --route-table-id ${PRIVATE_RT} \
  --subnet-id ${PRIVATE_SUBNET_1} \
  --region ${AWS_REGION}

aws ec2 associate-route-table \
  --route-table-id ${PRIVATE_RT} \
  --subnet-id ${PRIVATE_SUBNET_2} \
  --region ${AWS_REGION}

echo "✓ Private subnets associated with private route table"

# Save resource IDs to file
echo ""
echo "Step 7: Saving resource IDs..."
cat > ${OUTPUT_FILE} <<EOF
# AWS Resources Created
# Generated on: $(date)
# Region: ${AWS_REGION}

export AWS_REGION="${AWS_REGION}"
export VPC_ID="${VPC_ID}"
export IGW_ID="${IGW_ID}"
export PUBLIC_SUBNET_1="${PUBLIC_SUBNET_1}"
export PUBLIC_SUBNET_2="${PUBLIC_SUBNET_2}"
export PRIVATE_SUBNET_1="${PRIVATE_SUBNET_1}"
export PRIVATE_SUBNET_2="${PRIVATE_SUBNET_2}"
export PUBLIC_RT="${PUBLIC_RT}"
export PRIVATE_RT="${PRIVATE_RT}"
export NAT_GW="${NAT_GW}"
export EIP_ALLOC_ID="${EIP_ALLOC_ID}"
EOF

echo "✓ Resource IDs saved to ${OUTPUT_FILE}"

echo ""
echo "======================================"
echo "✓ VPC Setup Completed Successfully!"
echo "======================================"
echo ""
echo "Summary:"
echo "  VPC ID: ${VPC_ID}"
echo "  Public Subnets: ${PUBLIC_SUBNET_1}, ${PUBLIC_SUBNET_2}"
echo "  Private Subnets: ${PRIVATE_SUBNET_1}, ${PRIVATE_SUBNET_2}"
echo "  NAT Gateway: ${NAT_GW}"
echo ""
echo "Resource IDs saved to: ${OUTPUT_FILE}"
echo "Source this file to use these values:"
echo "  source ${OUTPUT_FILE}"
echo ""
echo "Next step: Run ./02-create-security-groups.sh"
