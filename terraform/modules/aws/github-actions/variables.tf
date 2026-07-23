variable "github_owner" {
  description = "GitHub repository owner"
  type        = string
}

variable "github_repository" {
  description = "GitHub repository name"
  type        = string
}

variable "github_branch" {
  description = "GitHub branch allowed to assume the AWS role"
  type        = string
  default     = "main"
}

variable "environment" {
  description = "Deployment environment"
  type        = string
}

variable "tags" {
  description = "Common tags applied to resources"
  type        = map(string)
  default     = {}
}