variable "state_bucket_name" {
  type        = string
  description = "S3 bucket name for Terraform state"
  default     = "ridebooking-terraform-state-2026"
}

variable "lock_table_name" {
  type        = string
  description = "DynamoDB table name for Terraform state locking"
  default     = "ridebooking-terraform-lock"
}
