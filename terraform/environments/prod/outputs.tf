output "vpc_id" {
  value = module.foundation.vpc_id
}

output "eks_cluster_name" {
  value = module.eks.cluster_name
}

output "eks_cluster_endpoint" {
  value = module.eks.cluster_endpoint
}

output "db_endpoint" {
  value = module.database.db_endpoint
}

output "ecr_repository_urls" {
  value = module.registry.repository_urls
}

output "payment_queue_url" {
  value = module.messaging.payment_queue_url
}

output "notification_queue_url" {
  value = module.messaging.notification_queue_url
}

output "irsa_service_account_role_arn" {
  value = module.eks.secrets_role_arn
}

output "app_runtime_secret_name" {
  value = aws_secretsmanager_secret.app_runtime.name
}

output "github_actions_role_arn" {
  description = "IAM role assumed by GitHub Actions through OIDC."
  value       = module.github_actions.role_arn
}

output "github_actions_oidc_provider_arn" {
  description = "GitHub Actions OIDC provider ARN."
  value       = module.github_actions.oidc_provider_arn
}

