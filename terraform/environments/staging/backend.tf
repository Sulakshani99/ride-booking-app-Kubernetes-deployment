terraform {
  backend "s3" {
    bucket         = "ridebooking-terraform-state-2026"
    key            = "staging/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "ridebooking-terraform-lock"
  }
}
