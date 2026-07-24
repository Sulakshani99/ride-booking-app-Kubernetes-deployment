# Ride Booking Microservices Application K8s Deployment on AWS EKS

A production-style DevOps project that deploys a Spring Boot based Ride Booking Application on **Amazon Elastic Kubernetes Service (EKS)** using **Terraform**, **GitHub Actions**, **Argo CD**, and **Kubernetes** following GitOps practices.

The project demonstrates the complete lifecycle of modern cloud-native application deployment, from infrastructure provisioning to automated application delivery, secrets management, monitoring, and autoscaling.

---

## Project Highlights

- Provision AWS infrastructure using Terraform
- Multi-environment deployment (Development, Staging and Production)
- GitOps-based Continuous Delivery with Argo CD
- CI pipeline using GitHub Actions
- Docker image storage in Amazon ECR
- Kubernetes deployment using Kustomize overlays
- Secure secrets management using AWS Secrets Manager and External Secrets Operator
- Application monitoring with Prometheus and Grafana
- Custom Prometheus alert rules for microservice health monitoring
- Horizontal Pod Autoscaler (HPA) for automatic scaling
- Rolling Update deployment strategy for zero/minimal downtime

---

## Architecture Overview

```
Developer
     │
     ▼
 GitHub Repository
     │
     ▼
 GitHub Actions
(Build → Test → Docker Image)
     │
     ▼
 Amazon ECR
     │
     ▼
 Update Kubernetes Manifests
     │
     ▼
 Argo CD (GitOps)
     │
     ▼
 Amazon EKS
     │
     ├──────────────► Prometheus
     │                    │
     │                    ▼
     │                 Grafana
     │
     └──────────────► External Secrets
                          │
                          ▼
                 AWS Secrets Manager
```

---

## Repository Structure

```text
.
├── .github/
│   └── workflows/              # CI/CD workflows
│
├── argocd/                     # Argo CD applications & project
│
├── kubernetes/
│   ├── base/                   # Common Kubernetes resources
│   └── environments/           # Dev, Staging & Production overlays
│
├── monitoring/                 # Monitoring configuration
│
├── terraform/
│   ├── bootstrap/              # Remote state backend
│   ├── modules/                # Reusable infrastructure modules
│   └── environments/           # Dev & Production infrastructure
│
└── README.md
```

---

## Infrastructure

Infrastructure is provisioned using **Terraform**.

The project includes:

- VPC
- Public & Private Subnets
- Internet Gateway
- NAT Gateway
- Amazon EKS Cluster
- Managed Node Groups
- Amazon RDS (MySQL)
- IAM Roles & Policies
- Amazon ECR
- S3 Remote Backend
- DynamoDB State Locking

---

## Kubernetes Deployment

Application deployment is managed using **Kustomize**.

The repository contains:

- Base Kubernetes manifests
- Development overlay
- Staging overlay
- Production overlay

Each environment customizes configuration while sharing the same base manifests.

---

## CI/CD Pipeline

GitHub Actions automates:

- Build Spring Boot services
- Build Docker images
- Push images to Amazon ECR
- Update Kubernetes manifests

Argo CD continuously watches the Git repository and synchronizes the changes to Amazon EKS.

---

## Monitoring & Observability

The monitoring stack includes:

- Prometheus
- Grafana
- ServiceMonitors
- Custom Prometheus Rules

Application metrics are exposed using Spring Boot Actuator and Micrometer.

---

## Secrets Management

Sensitive information is **not stored in Git**.

Secrets are managed using:

- AWS Secrets Manager
- External Secrets Operator

which automatically synchronizes secrets into Kubernetes.

---

## Autoscaling

Each microservice is configured with a Horizontal Pod Autoscaler (HPA) to automatically adjust the number of running pods based on resource utilization.

---

## Deployment Strategy

This project uses Kubernetes **Rolling Update** deployments to ensure minimal downtime during application updates.

---

## Technologies

| Category | Technologies |
|-----------|--------------|
| Cloud | AWS |
| Infrastructure | Terraform |
| Container | Docker |
| Orchestration | Kubernetes (Amazon EKS) |
| GitOps | Argo CD |
| CI/CD | GitHub Actions |
| Registry | Amazon ECR |
| Monitoring | Prometheus, Grafana |
| Secrets | AWS Secrets Manager, External Secrets Operator |
| Autoscaling | Kubernetes HPA |
| Application | Spring Boot |

---

## Future Improvements

- AlertManager email notifications
- Centralized logging (Loki / ELK)
- Canary deployments
- Service Mesh
- Automated backup and disaster recovery

---

## Author

**Sulakshani Tashina**

BSc (Hons) Computer Engineering  
University of Ruhuna
