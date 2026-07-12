resource "aws_ecr_repository" "main" {
  for_each             = toset(var.repositories)
  name                 = "${var.environment}-${each.key}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = var.tags
}
