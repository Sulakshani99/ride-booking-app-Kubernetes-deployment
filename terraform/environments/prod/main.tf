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

  node_desired_size   = 3
  node_max_size       = 6
  node_min_size       = 2
  node_instance_types = ["t3.small"]

  tags = {
    Environment = var.environment
    Project     = "ride-booking-app"
    ManagedBy   = "terraform"
  }
}

# =========================================================
# Give GitHub Actions access to the EKS Kubernetes API
# =========================================================

resource "aws_eks_access_entry" "github_actions" {
  cluster_name  = module.eks.cluster_name
  principal_arn = module.github_actions.role_arn
  type          = "STANDARD"

  depends_on = [
    module.eks,
    module.github_actions
  ]
}

resource "aws_eks_access_policy_association" "github_actions_admin" {
  cluster_name  = module.eks.cluster_name
  principal_arn = module.github_actions.role_arn

  policy_arn = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"

  access_scope {
    type = "cluster"
  }

  depends_on = [
    aws_eks_access_entry.github_actions
  ]
}

module "database" {
  source = "../../modules/aws/database"

  environment                   = var.environment
  db_instance_class             = "db.t3.micro"
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

module "github_actions" {
  source = "../../modules/aws/github-actions"

  github_owner      = "Sulakshani99"
  github_repository = "ride-booking-app-Kubernetes-deployment"
  github_branch     = "main"

  environment = var.environment

  tags = var.tags
}

resource "aws_iam_role_policy" "sqs_access" {
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
