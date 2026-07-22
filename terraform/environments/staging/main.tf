module "foundation" {
  source = "../../modules/aws/foundation"

  environment          = var.environment
  vpc_cidr             = var.vpc_cidr
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  availability_zones   = var.availability_zones

  tags = {
    Environment = var.environment
    Project     = "ride-booking-app"
    ManagedBy   = "terraform"
  }
}

module "eks" {
  source = "../../modules/aws/eks"

  environment        = var.environment
  cluster_name       = var.cluster_name
  kubernetes_version = "1.34"

  vpc_id             = module.foundation.vpc_id
  private_subnet_ids = module.foundation.private_subnet_ids

  node_desired_size   = 2
  node_max_size       = 4
  node_min_size       = 1
  node_instance_types = ["t3.small"]

  tags = {
    Environment = var.environment
    Project     = "ride-booking-app"
    ManagedBy   = "terraform"
  }
}

module "database" {
  source = "../../modules/aws/database"

  environment                   = var.environment
  db_instance_class             = "db.t3.micro"
  db_name                       = "ridebooking_db"
  private_subnet_ids            = module.foundation.private_subnet_ids
  vpc_id                        = module.foundation.vpc_id
  db_username                   = var.db_username
  db_password                   = var.db_password
  eks_cluster_security_group_id = module.eks.cluster_security_group_id

  tags = {
    Environment = var.environment
    Project     = "ride-booking-app"
    ManagedBy   = "terraform"
  }
}

module "registry" {
  source = "../../modules/aws/registry"

  environment = var.environment

  tags = {
    Environment = var.environment
    Project     = "ride-booking-app"
    ManagedBy   = "terraform"
  }
}

module "messaging" {
  source = "../../modules/aws/messaging"

  environment = var.environment

  tags = {
    Environment = var.environment
    Project     = "ride-booking-app"
    ManagedBy   = "terraform"
  }
}

resource "aws_iam_role_policy" "app_runtime_access" {
  name = "${var.environment}-ridebooking-app-runtime-access"
  role = module.eks.secrets_role_id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "sqs:SendMessage",
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ]
        Resource = [
          module.messaging.payment_queue_arn,
          module.messaging.notification_queue_arn
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "ses:SendEmail",
          "ses:SendRawEmail"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:DescribeSecret"
        ]
        Resource = [
          module.database.db_secret_arn,
          aws_secretsmanager_secret.app_runtime.arn
        ]
      }
    ]
  })
}
