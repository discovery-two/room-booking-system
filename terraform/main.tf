terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

resource "aws_ecr_repository" "ecr-repo" {
  name                 = "${var.proj_name}-ecr"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

data "aws_ecr_lifecycle_policy_document" "expire_policy" {
  rule {
    priority    = 1
    description = "Keep only 3 most recent dev/test images"

    action {
      type = "expire"
    }

    selection {
      tag_status       = "tagged"
      tag_pattern_list = ["dev*"]
      count_type       = "imageCountMoreThan"
      count_number     = 3
    }
  }

  rule {
    priority    = 2
    description = "Keep only 2 most recent prod images"

    action {
      type = "expire"
    }

    selection {
      tag_status       = "tagged"
      tag_pattern_list = ["prod*"]
      count_type       = "imageCountMoreThan"
      count_number     = 2
    }
  }

  rule {
    priority    = 3
    description = "Expire untagged images after a day"

    action {
      type = "expire"
    }

    selection {
      tag_status   = "untagged"
      count_type   = "sinceImagePushed"
      count_number = 1
      count_unit   = "days"
    }
  }
}

resource "aws_ecr_lifecycle_policy" "ecr-lifecycle-policy" {
  repository = aws_ecr_repository.ecr-repo.name

  policy = data.aws_ecr_lifecycle_policy_document.expire_policy.json
}

resource "aws_ecs_cluster" "ecs-cluster" {
  name = "${var.proj_name}-ecs-cluster"
  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}
