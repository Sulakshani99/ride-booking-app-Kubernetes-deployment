data "tls_certificate" "github_actions" {
  url = "https://token.actions.githubusercontent.com"
}

# =========================================================
# GitHub Actions OIDC provider
# =========================================================

resource "aws_iam_openid_connect_provider" "github_actions" {
  url = "https://token.actions.githubusercontent.com"

  client_id_list = [
    "sts.amazonaws.com"
  ]

  thumbprint_list = [
    data.tls_certificate.github_actions.certificates[0].sha1_fingerprint
  ]

  tags = merge(
    var.tags,
    {
      Name        = "${var.environment}-github-actions-oidc"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  )
}

# =========================================================
# GitHub Actions trust policy
# =========================================================

data "aws_iam_policy_document" "github_actions_assume_role" {
  statement {
    sid    = "AllowGitHubActionsOIDC"
    effect = "Allow"

    actions = [
      "sts:AssumeRoleWithWebIdentity"
    ]

    principals {
      type = "Federated"

      identifiers = [
        aws_iam_openid_connect_provider.github_actions.arn
      ]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"

      values = [
        "sts.amazonaws.com"
      ]
    }

    # Only the configured branch of the configured repository
    # can assume this AWS role.
    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:sub"

      values = [
        "repo:${var.github_owner}/${var.github_repository}:ref:refs/heads/${var.github_branch}"
      ]
    }
  }
}

# =========================================================
# GitHub Actions IAM role
# =========================================================

resource "aws_iam_role" "github_actions" {
  name = "${var.environment}-ridebooking-github-actions-role"

  description = "Allows GitHub Actions to manage ride-booking AWS infrastructure through Terraform and push images to ECR."

  assume_role_policy = data.aws_iam_policy_document.github_actions_assume_role.json

  max_session_duration = 3600

  tags = merge(
    var.tags,
    {
      Name        = "${var.environment}-ridebooking-github-actions-role"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  )
}

# =========================================================
# AWS-managed permissions
#
# PowerUserAccess:
# EC2, VPC, EKS, ECR, RDS, S3, DynamoDB,
# Secrets Manager, SQS, ELB, CloudWatch, etc.
#
# IAMFullAccess:
# IAM roles, policies, OIDC providers and PassRole.
# =========================================================

locals {
  github_actions_managed_policy_arns = {
    power_user = "arn:aws:iam::aws:policy/PowerUserAccess"
    iam_full   = "arn:aws:iam::aws:policy/IAMFullAccess"
  }
}

resource "aws_iam_role_policy_attachment" "github_actions_managed_policies" {
  for_each = local.github_actions_managed_policy_arns

  role       = aws_iam_role.github_actions.name
  policy_arn = each.value
}