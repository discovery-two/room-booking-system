output "ecr_repository_url" {
  value = aws_ecr_repository.ecr-repo.repository_url
}

output "ecr_repository_name" {
  value = aws_ecr_repository.ecr-repo.name
}

output "ecs_cluster_name" {
  value = aws_ecs_cluster.ecs-cluster.name
}

output "ecs_cluster_arn" {
  value = aws_ecs_cluster.ecs-cluster.arn
}

output "ecs_execution_role_arn" {
  value = aws_iam_role.ecs-execution-role.arn
}

output "aws_region" {
  value = var.aws_region
}

output "project_name" {
  value = var.proj_name
}

output "load_balancer_url" {
  value = "http://${aws_lb.main.dns_name}"
}

output "database_endpoint" {
  value = aws_db_instance.postgres.endpoint
}

output "database_secret_arn" {
  value = aws_db_instance.postgres.master_user_secret[0].secret_arn
}

output "cognito_user_pool_id" {
  value = aws_cognito_user_pool.roombook_pool.id
}

output "cognito_user_pool_arn" {
  value = aws_cognito_user_pool.roombook_pool.arn
}

output "cognito_client_id" {
  value = aws_cognito_user_pool_client.roombook_client.id
}

output "cognito_jwk_set_uri" {
  value = "https://cognito-idp.${var.aws_region}.amazonaws.com/${aws_cognito_user_pool.roombook_pool.id}/.well-known/jwks.json"
}
