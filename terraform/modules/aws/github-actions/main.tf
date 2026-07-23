data "aws_caller_identity" "current" {}

# GitHub and EKS use separate OIDC issuers.
# EKS:
# https://oidc.eks.<region>.amazonaws.com/id/<cluster-id>
# GitHub:
# https://token.actions.githubusercontent.com

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

    # AWS STS must be the expected audience.
    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"

      values = [
        "sts.amazonaws.com"
      ]
    }

    # Only the main branch of this exact repository can assume the role.
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

  description = "Allows GitHub Actions to push ride-booking images to Amazon ECR."

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

data "aws_iam_policy_document" "github_actions_ecr" {
  # ECR login requires GetAuthorizationToken with Resource = "*".
  statement {
    sid    = "AllowECRAuthentication"
    effect = "Allow"

    actions = [
      "ecr:GetAuthorizationToken"
    ]

    resources = ["*"]
  }

  # Limit image operations to the four ride-booking repositories.
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
  description = "Allows GitHub Actions to build and push ride-booking images."
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