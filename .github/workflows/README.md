# GitHub Actions Workflows

This directory contains the CI/CD workflows used to build the Ride Booking microservices, push Docker images to Amazon ECR, update Kubernetes production manifests, and manage Terraform infrastructure.

## Workflow Files

```text
.github/workflows/
├── ci.yml
├── reusable-service-build.yml
├── terraform.yml
└── README.md
```

---

## 1. Application CI/CD Pipeline

File:

```text
ci.yml
```

This is the main application pipeline.

It runs when:

- Code is pushed to the `main` branch
- The workflow is started manually

### Pipeline Flow

```text
Push to main
      │
      ▼
Detect changed services
      │
      ▼
Run tests for changed services
      │
      ▼
Build Docker images
      │
      ▼
Push images to Amazon ECR
      │
      ▼
Update production Kustomize image references
      │
      ▼
Commit changes to Git
      │
      ▼
Argo CD deploys the new versions to Amazon EKS
```

### Change Detection

The pipeline checks which service folders were changed:

- `auth-service/`
- `ride-service/`
- `payment-service/`
- `notification-service/`

Only changed services are built.

For example, when only files inside `payment-service/` are modified, the pipeline builds and pushes only the Payment Service image.

A manually triggered workflow builds all four services.

### Production Manifest Update

After successful image builds, the workflow updates:

```text
kubernetes/environments/prod/kustomization.yaml
```

Each new Docker image uses the Git commit SHA as its image tag.

Example:

```text
123456789.dkr.ecr.us-east-1.amazonaws.com/prod-auth-service:<commit-sha>
```

The updated production manifest is committed back to the `main` branch using the GitHub Actions bot.

Argo CD detects this Git change and synchronizes the updated manifests with the EKS cluster.

---

## 2. Reusable Microservice Build

File:

```text
reusable-service-build.yml
```

This reusable workflow contains the common build process used by all four microservices.

The main pipeline passes the following values:

- Service name
- Service directory
- Amazon ECR repository name

### Build Steps

For each selected service, the workflow:

1. Checks out the repository
2. Configures Java 17
3. Starts a MySQL 8 container for tests
4. Runs Maven tests
5. Authenticates with AWS using GitHub OIDC
6. Logs in to Amazon ECR
7. Builds the Docker image
8. Pushes the image to ECR
9. Returns the full image URI to the main workflow

The tests use temporary CI values for the database, JWT secret, SQS queues, and SES sender address.

These values are used only during the GitHub Actions test process.

---

## 3. Terraform Pipeline

File:

```text
terraform.yml
```

This workflow validates and manages the production Terraform infrastructure.

The Terraform working directory is:

```text
terraform/environments/prod
```

### Automatic Execution

For pull requests and pushes to `main`, the workflow runs when files change inside:

```text
terraform/**
```

It performs:

```text
terraform fmt -check
terraform init
terraform validate
terraform plan
```

This helps identify formatting, validation, and infrastructure problems before applying changes.

### Manual Actions

The workflow can also be started manually with one of the following actions:

- `plan`
- `apply`
- `destroy`

`apply` and `destroy` are not automatically executed on every push. They must be selected manually through GitHub Actions.

---

## AWS Authentication

The workflows authenticate with AWS using OpenID Connect instead of storing permanent AWS access keys in GitHub.

The AWS IAM role is provided through the GitHub repository variable:

```text
AWS_GITHUB_ACTIONS_ROLE_ARN
```

The workflows use the following permission:

```yaml
permissions:
  id-token: write
```

This allows GitHub Actions to request a temporary AWS identity token and assume the configured IAM role.

---

## Required GitHub Configuration

### Repository Variable

Configure this under:

```text
GitHub Repository
→ Settings
→ Secrets and variables
→ Actions
→ Variables
```

Required variable:

```text
AWS_GITHUB_ACTIONS_ROLE_ARN
```

It should contain the ARN of the AWS IAM role that GitHub Actions is allowed to assume.

### Repository Secrets

The Terraform workflow expects these GitHub secrets:

```text
DB_PASSWORD_PROD
JWT_SECRET_PROD
ADMIN_PASSWORD_PROD
```

These values are passed to Terraform as:

```text
TF_VAR_db_password
TF_VAR_jwt_secret
TF_VAR_admin_password
```

Sensitive values should never be committed directly to the repository.

---

## Amazon ECR Repositories

The application pipeline pushes images to the following production repositories:

```text
prod-auth-service
prod-ride-service
prod-payment-service
prod-notification-service
```

The workflow uses:

```text
us-east-1
```

as the AWS region.

---

## Concurrency Control

The workflows use concurrency groups to prevent conflicting executions.

The application workflow prevents multiple runs from updating production manifests at the same time.

The Terraform workflow prevents multiple production infrastructure operations from running simultaneously.

---

## GitOps Deployment

GitHub Actions does not directly deploy the application with `kubectl`.

Instead, it updates the production Kubernetes manifests in Git.

```text
GitHub Actions
      │
      ▼
Update production image tags
      │
      ▼
Commit changes to main
      │
      ▼
Argo CD detects the commit
      │
      ▼
Argo CD synchronizes Amazon EKS
```

This follows a GitOps deployment model where Git is the source of truth for the Kubernetes environment.

---

## Running Workflows Manually

Open:

```text
GitHub Repository
→ Actions
```

### Run the complete application pipeline

Select:

```text
Ride Booking CI Pipeline
```

Then choose:

```text
Run workflow
```

A manual run builds all four microservices.

### Run Terraform

Select:

```text
Terraform Pipeline
```

Choose one of:

```text
plan
apply
destroy
```

Use `destroy` carefully because it removes the Terraform-managed production infrastructure.
