variable "environment" {
  type        = string
  description = "Environment name"
}

variable "domain_name" {
  type        = string
  description = "Root domain name (e.g., example.com)"
}

variable "subdomain" {
  type        = string
  description = "App subdomain (e.g., api)"
  default     = "api"
}

variable "create_records" {
  type        = bool
  description = "Whether to create Route53 records"
  default     = false
}

variable "tags" {
  type    = map(string)
  default = {}
}
