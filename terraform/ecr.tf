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
    description = "Keep 10 images max"

    action {
      type = "expire"
    }

    selection {
      tag_status   = "any"
      count_type   = "imageCountMoreThan"
      count_number = 10
    }
  }
}

resource "aws_ecr_lifecycle_policy" "ecr-lifecycle-policy" {
  repository = aws_ecr_repository.ecr-repo.name

  policy = data.aws_ecr_lifecycle_policy_document.expire_policy.json
}
