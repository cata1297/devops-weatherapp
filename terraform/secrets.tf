resource "aws_secretsmanager_secret" "openweather" {
  name = "${var.project_name}-openweather-api-key"

  tags = {
    Project = var.project_name
  }
}
