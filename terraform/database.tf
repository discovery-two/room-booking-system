resource "aws_db_subnet_group" "main" {
  name       = "${var.proj_name}-db-subnet-group"
  subnet_ids = [aws_subnet.public_a.id, aws_subnet.public_b.id]

  tags = {
    Name = "${var.proj_name} DB subnet group"
  }
}

resource "aws_db_instance" "postgres" {
  identifier     = "${var.proj_name}-db"
  engine         = "postgres"
  engine_version = "15.7"
  instance_class = "db.t3.micro"

  allocated_storage     = 20
  max_allocated_storage = 100
  storage_type          = "gp2"
  storage_encrypted     = true

  db_name = "roombook"

  manage_master_user_password = true
  username                    = "pgadmin"

  vpc_security_group_ids = [aws_security_group.main.id]
  db_subnet_group_name   = aws_db_subnet_group.main.name

  skip_final_snapshot = true
  deletion_protection = false

  tags = {
    Name = "${var.proj_name} PostgreSQL"
  }
}
