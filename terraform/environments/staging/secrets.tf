resource "aws_secretsmanager_secret" "app_runtime" {
  name                    = "${var.environment}/ridebooking/app"
  recovery_window_in_days = 0
}

resource "aws_secretsmanager_secret_version" "app_runtime" {
  secret_id = aws_secretsmanager_secret.app_runtime.id
  secret_string = jsonencode({
    jwt_secret             = var.jwt_secret
    admin_password         = var.admin_password
    aws_region             = var.aws_region
    payment_queue_url      = module.messaging.payment_queue_url
    notification_queue_url = module.messaging.notification_queue_url
    ses_from               = var.ses_from
  })
}
