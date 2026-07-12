resource "aws_security_group" "rds" {
  name_prefix = "${var.environment}-rds-sg-"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [var.eks_cluster_security_group_id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = var.tags
}

resource "aws_db_instance" "main" {
  identifier          = "${var.environment}-ridebooking-db"
  engine              = "mysql"
  engine_version      = "8.0"
  instance_class      = var.db_instance_class
  allocated_storage   = 20
  storage_type        = "gp3"
  db_name             = var.db_name
  username            = var.db_username
  password            = var.db_password
  skip_final_snapshot = true
  apply_immediately   = true

  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name   = aws_db_subnet_group.main.name

  tags = var.tags
}

resource "aws_secretsmanager_secret" "db_secret" {
  name                    = "${var.environment}/ridebooking/db"
  recovery_window_in_days = 0

  tags = var.tags
}

resource "aws_secretsmanager_secret_version" "db_secret_val" {
  secret_id = aws_secretsmanager_secret.db_secret.id
  secret_string = jsonencode({
    username = var.db_username
    password = var.db_password
    host     = aws_db_instance.main.address
    port     = aws_db_instance.main.port
    database = var.db_name
  })
}
