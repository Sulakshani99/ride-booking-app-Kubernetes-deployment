variable "environment" {
  description = "Deployment environment."
  type        = string
}

variable "github_owner" {
  description = "GitHub user or organization that owns the repository."
  type        = string
}

variable "github_repository" {
  description = "GitHub repository name without the owner."
  type        = string
}

variable "github_branch" {
  description = "GitHub branch allowed to assume the AWS IAM role."
  type        = string
  default     = "main"
}

variable "aws_region" {
  description = "AWS region containing the ECR repositories."
  type        = string
}

variable "ecr_repository_names" {
  description = "ECR repositories GitHub Actions may push images into."
  type        = set(string)
}

variable "tags" {
  description = "Common tags applied to AWS resources."
  type        = map(string)
  default     = {}
}

variable "terraform_state_bucket_name" {
  description = "S3 bucket containing the Terraform remote state"
  type        = string
}

variable "terraform_lock_table_name" {
  description = "DynamoDB table used for Terraform state locking"
  type        = string
}