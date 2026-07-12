aws_region   = "us-east-1"
environment  = "dev"
cluster_name = "ridebooking"

vpc_cidr             = "10.1.0.0/16"
public_subnet_cidrs  = ["10.1.1.0/24", "10.1.2.0/24"]
private_subnet_cidrs = ["10.1.10.0/24", "10.1.11.0/24"]
availability_zones   = ["us-east-1a", "us-east-1b"]

db_username = "ride_admin"
# db_password should be passed securely via CLI or environment variable.
