data "aws_caller_identity" "current" {}

data "tls_certificate" "github_actions" {
  url = "https://token.actions.githubusercontent.com"
}

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

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:sub"

      values = [
        "repo:${var.github_owner}/${var.github_repository}:ref:refs/heads/${var.github_branch}"
      ]
    }
  }
}

resource "aws_iam_role" "github_actions" {
  name = "${var.environment}-ridebooking-github-actions-role"

  description = "Allows GitHub Actions to push images to ECR and manage Terraform."

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
# ECR permissions
# =========================================================

data "aws_iam_policy_document" "github_actions_ecr" {
  statement {
    sid    = "AllowECRAuthentication"
    effect = "Allow"

    actions = [
      "ecr:GetAuthorizationToken"
    ]

    resources = ["*"]
  }

  statement {
    sid    = "AllowRideBookingECRPushAndRead"
    effect = "Allow"

    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:BatchGetImage",
      "ecr:CompleteLayerUpload",
      "ecr:GetDownloadUrlForLayer",
      "ecr:InitiateLayerUpload",
      "ecr:ListImages",
      "ecr:PutImage",
      "ecr:UploadLayerPart"
    ]

    resources = [
      for repository_name in var.ecr_repository_names :
      "arn:aws:ecr:${var.aws_region}:${data.aws_caller_identity.current.account_id}:repository/${repository_name}"
    ]
  }
}

resource "aws_iam_policy" "github_actions_ecr" {
  name        = "${var.environment}-ridebooking-github-actions-ecr"
  description = "Allows GitHub Actions to push ride-booking images."
  policy      = data.aws_iam_policy_document.github_actions_ecr.json

  tags = merge(
    var.tags,
    {
      Name        = "${var.environment}-ridebooking-github-actions-ecr"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  )
}

resource "aws_iam_role_policy_attachment" "github_actions_ecr" {
  role       = aws_iam_role.github_actions.name
  policy_arn = aws_iam_policy.github_actions_ecr.arn
}

# =========================================================
# Terraform S3 backend permissions
# =========================================================

data "aws_iam_policy_document" "github_actions_terraform_backend" {
  statement {
    sid    = "AllowTerraformStateBucketAccess"
    effect = "Allow"

    actions = [
      "s3:ListBucket",
      "s3:GetBucketLocation"
    ]

    resources = [
      "arn:aws:s3:::${var.terraform_state_bucket_name}"
    ]
  }

  statement {
    sid    = "AllowTerraformStateObjectAccess"
    effect = "Allow"

    actions = [
      "s3:GetObject",
      "s3:PutObject",
      "s3:DeleteObject"
    ]

    resources = [
      "arn:aws:s3:::${var.terraform_state_bucket_name}/*"
    ]
  }
}

resource "aws_iam_policy" "github_actions_terraform_backend" {
  name        = "${var.environment}-ridebooking-github-actions-terraform-backend"
  description = "Allows GitHub Actions to access the Terraform S3 backend."
  policy      = data.aws_iam_policy_document.github_actions_terraform_backend.json

  tags = merge(
    var.tags,
    {
      Name        = "${var.environment}-ridebooking-github-actions-terraform-backend"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  )
}

resource "aws_iam_role_policy_attachment" "github_actions_terraform_backend" {
  role       = aws_iam_role.github_actions.name
  policy_arn = aws_iam_policy.github_actions_terraform_backend.arn
}