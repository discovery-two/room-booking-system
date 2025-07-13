resource "aws_cloudwatch_log_group" "ecs_logs" {
  name              = "/ecs/roombook"
  retention_in_days = 7
}

resource "aws_ecs_cluster" "ecs-cluster" {
  name = "${var.proj_name}-ecs-cluster"
  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

resource "aws_ecs_task_definition" "ecs-task-def" {
  family                   = "roombook-dev"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "1024"
  memory                   = "2048"
  execution_role_arn       = aws_iam_role.ecs-execution-role.arn

  container_definitions = jsonencode([{
    name  = "roombook"
    image = "${aws_ecr_repository.ecr-repo.repository_url}:latest"
    portMappings = [{
      containerPort = 8080
    }]
    environment = [
      {
        name  = "DATABASE_URL"
        value = "jdbc:postgresql://${aws_db_instance.postgres.endpoint}/${aws_db_instance.postgres.db_name}"
      },
      {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "prod"
      }
    ]
    secrets = [
      {
        name      = "DATABASE_USERNAME"
        valueFrom = "${aws_db_instance.postgres.master_user_secret[0].secret_arn}:username::"
      },
      {
        name      = "DATABASE_PASSWORD"
        valueFrom = "${aws_db_instance.postgres.master_user_secret[0].secret_arn}:password::"
      }
    ]
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.ecs_logs.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }])

  lifecycle {
    ignore_changes = all
  }
}

resource "aws_ecs_service" "ecs-service" {
  name            = "roombook-serv"
  cluster         = aws_ecs_cluster.ecs-cluster.id
  task_definition = "roombook-dev"
  desired_count   = 2
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = [aws_subnet.public_a.id, aws_subnet.public_b.id]
    security_groups  = [aws_security_group.main.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.ecs.arn
    container_name   = "roombook"
    container_port   = 8080
  }

  depends_on = [aws_lb_listener.https]

  lifecycle {
    ignore_changes = [task_definition]
  }
}
