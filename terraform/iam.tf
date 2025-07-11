data "aws_iam_policy_document" "ecs-role-trust-policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs-execution-role" {
  name               = "${var.proj_name}-ecs-exec"
  assume_role_policy = data.aws_iam_policy_document.ecs-role-trust-policy.json
}

data "aws_iam_policy_document" "ecs-execution-policy" {
  statement {
    actions = [
      "ecr:GetAuthorizationToken",
      "ecr:BatchCheckLayerAvailability",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage",
      "logs:CreateLogStream",
      "logs:PutLogEvents",
      "secretsmanager:GetSecretValue"
    ]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "ecs-execution-policy" {
  role   = aws_iam_role.ecs-execution-role.id
  policy = data.aws_iam_policy_document.ecs-execution-policy.json
}
