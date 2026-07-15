resource "aws_sqs_queue" "payment_dlq" {
  name = "${var.environment}-ride-payment-events-dlq"
  tags = var.tags
}

resource "aws_sqs_queue" "payment_events" {
  name                       = "${var.environment}-ride-payment-events"
  visibility_timeout_seconds = 30
  message_retention_seconds  = 86400

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.payment_dlq.arn
    maxReceiveCount     = 5
  })

  tags = var.tags
}

resource "aws_sqs_queue" "notification_dlq" {
  name = "${var.environment}-ride-notification-events-dlq"
  tags = var.tags
}

resource "aws_sqs_queue" "notification_events" {
  name                       = "${var.environment}-ride-notification-events"
  visibility_timeout_seconds = 30
  message_retention_seconds  = 86400

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.notification_dlq.arn
    maxReceiveCount     = 5
  })

  tags = var.tags
}