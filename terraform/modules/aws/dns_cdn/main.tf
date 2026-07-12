data "aws_route53_zone" "selected" {
  count        = var.create_records ? 1 : 0
  name         = var.domain_name
  private_zone = false
}

resource "aws_route53_record" "api" {
  count   = var.create_records ? 1 : 0
  zone_id = data.aws_route53_zone.selected[0].zone_id
  name    = "${var.subdomain}.${var.domain_name}"
  type    = "CNAME"
  ttl     = 300
  records = ["placeholder-lb.example.com"]
}
