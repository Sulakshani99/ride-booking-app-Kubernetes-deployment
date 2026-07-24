# Kubernetes Configuration

This directory contains all Kubernetes manifests used to deploy the Ride Booking Application on **Amazon Elastic Kubernetes Service (EKS)**.

The project follows a **Kustomize base + overlays** structure, enabling reusable Kubernetes resources while maintaining separate configurations for **Development**, **Staging**, and **Production** environments.

---

## Directory Structure

```text
kubernetes/
├── base/
│   ├── apps/                 # Application deployments
│   ├── autoscaling/          # Horizontal Pod Autoscalers
│   ├── config/              # ConfigMaps
│   ├── external-secrets/    # External Secrets configuration
│   ├── ingress/             # ALB Ingress resources
│   ├── monitoring/          # ServiceMonitors & Prometheus Rules
│   ├── namespace/           # Namespace definition
│   ├── services/            # Kubernetes Services
│   └── kustomization.yaml
│
├── environments/
│   ├── dev/
│   ├── staging/
│   └── prod/
│
└── README.md
```

---

## Application Architecture

The Ride Booking Application consists of four Spring Boot microservices deployed on Kubernetes.

- **Auth Service** – User authentication and authorization
- **Ride Service** – Ride booking and management
- **Payment Service** – Payment processing
- **Notification Service** – Email and notification handling

The services communicate within the Kubernetes cluster and connect securely to an Amazon RDS MySQL database.

---

## Base Configuration

The `base` directory contains reusable Kubernetes manifests shared by all environments.

Resources include:

- Deployments
- Services
- ConfigMaps
- Namespace
- ALB Ingress
- Horizontal Pod Autoscalers (HPA)
- External Secrets
- Monitoring resources
- Kustomization file

These manifests define the common application configuration while keeping environment-specific settings separate.

---

## Environment Overlays

The `environments` directory contains Kustomize overlays for each deployment environment.

| Environment | Purpose |
|-------------|---------|
| **dev** | Development and testing |
| **staging** | Pre-production validation |
| **prod** | Production deployment |

Each overlay customizes the shared base configuration by applying environment-specific values such as:

- Docker image tags
- Replica counts
- Resource requests and limits
- Environment variables
- Configuration overrides

This approach minimizes duplication while ensuring consistency across environments.

---

## Secrets Management

Sensitive information is **never stored in Git**.

Application secrets are securely managed using:

- AWS Secrets Manager
- External Secrets Operator

The External Secrets Operator automatically synchronizes secrets from AWS Secrets Manager into Kubernetes Secrets, allowing applications to securely access database credentials and other sensitive configuration.

---

## Ingress

Application traffic is routed through an **AWS Application Load Balancer (ALB)** using Kubernetes Ingress resources.

The ingress configuration provides a single entry point for external traffic and routes requests to the appropriate microservice.

---

## Autoscaling

Each microservice is configured with a **Horizontal Pod Autoscaler (HPA)**.

The HPA automatically increases or decreases the number of running pods based on resource utilization, improving application performance and resource efficiency.

---

## Monitoring

The project includes Kubernetes resources for monitoring and observability using:

- Prometheus
- Grafana
- ServiceMonitors
- Prometheus Rules

Application metrics are exposed using Spring Boot Actuator and Micrometer, collected by Prometheus, and visualized through Grafana dashboards.

---

## GitOps Deployment Workflow

Application deployments follow a GitOps workflow using GitHub Actions and Argo CD.

```text
Developer
     │
     ▼
GitHub Repository
     │
     ▼
GitHub Actions
(Build & Push Docker Images)
     │
     ▼
Update Kubernetes Manifests
     │
     ▼
Argo CD
     │
     ▼
Amazon EKS
```

Argo CD continuously monitors this repository and automatically synchronizes Kubernetes manifests with the EKS cluster whenever changes are detected.

---

## Deployment Strategy

The application uses Kubernetes **Rolling Update** deployments, ensuring minimal downtime during application updates by gradually replacing old pods with new ones.

---

## Deploy Using Kustomize

Deploy a specific environment:

### Development

```bash
kubectl apply -k environments/dev
```

### Staging

```bash
kubectl apply -k environments/staging
```

### Production

```bash
kubectl apply -k environments/prod
```

---

## Verify Deployment

Check application pods:

```bash
kubectl get pods -n ridebooking
```

Check services:

```bash
kubectl get svc -n ridebooking
```

Check ingress:

```bash
kubectl get ingress -n ridebooking
```

Check Horizontal Pod Autoscalers:

```bash
kubectl get hpa -n ridebooking
```

---

## Related Components

This Kubernetes configuration integrates with the following project components:

- **Terraform** – Provisions AWS infrastructure (VPC, EKS, RDS, ECR, IAM)
- **GitHub Actions** – Builds, tests, and publishes Docker images
- **Amazon ECR** – Stores Docker container images
- **Argo CD** – Deploys applications using GitOps
- **AWS Secrets Manager** – Securely stores application secrets
- **Prometheus & Grafana** – Monitor application and cluster health
