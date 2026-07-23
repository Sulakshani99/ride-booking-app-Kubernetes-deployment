output "role_name" {
  description = "GitHub Actions IAM role name."
  value       = aws_iam_role.github_actions.name
}

output "role_arn" {
  description = "GitHub Actions IAM role ARN."
  value       = aws_iam_role.github_actions.arn
}

output "oidc_provider_arn" {
  description = "GitHub Actions OIDC provider ARN."
  value       = aws_iam_openid_connect_provider.github_actions.arn
}

output "ecr_policy_arn" {
  description = "IAM policy ARN used for ECR operations."
  value       = aws_iam_policy.github_actions_ecr.arn
}