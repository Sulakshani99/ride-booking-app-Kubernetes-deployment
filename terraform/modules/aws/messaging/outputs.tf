output "payment_queue_url" {
  value = aws_sqs_queue.payment_events.id
}

output "payment_queue_arn" {
  value = aws_sqs_queue.payment_events.arn
}

output "notification_queue_url" {
  value = aws_sqs_queue.notification_events.id
}

output "notification_queue_arn" {
  value = aws_sqs_queue.notification_events.arn
}