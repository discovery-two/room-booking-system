variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "proj_name" {
  type    = string
  default = "roombook"
}

variable "domain_name" {
  type      = string
  sensitive = true
}

variable "ssl_certificate_arn" {
  type      = string
  sensitive = true
}
