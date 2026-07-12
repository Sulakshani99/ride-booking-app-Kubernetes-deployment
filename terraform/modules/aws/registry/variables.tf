variable "environment" {
  type = string
}

variable "repositories" {
  type = list(string)
  default = [
    "auth-service",
    "ride-service",
    "payment-service",
    "notification-service"
  ]
}

variable "tags" {
  type    = map(string)
  default = {}
}
