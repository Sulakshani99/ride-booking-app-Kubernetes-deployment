# Ride Booking App - Kubernetes Deployment

This directory contains Kubernetes manifests organized using **Kustomize** for environment-specific deployments.

## Directory Structure

```
kubernetes/
├── base/                              # Base manifests shared across all environments
│   ├── infrastructure/                # Cluster infrastructure (namespace, serviceaccount)
│   │   ├── namespace.yaml
│   │   └── serviceaccount.yaml
│   ├── services/                      # Microservices deployments
│   │   ├── mysql/
│   │   │   ├── deployment.yaml
│   │   │   ├── service.yaml
│   │   │   └── pvc.yaml
│   │   ├── auth-service/
│   │   │   ├── deployment.yaml
│   │   │   ├── service.yaml
│   │   │   └── configmap.yaml
│   │   ├── ride-service/
│   │   │   ├── deployment.yaml
│   │   │   ├── service.yaml
│   │   │   └── configmap.yaml
│   │   ├── payment-service/
│   │   │   ├── deployment.yaml
│   │   │   ├── service.yaml
│   │   │   └── configmap.yaml
│   │   └── notification-service/
│   │       ├── deployment.yaml
│   │       ├── service.yaml
│   │       └── configmap.yaml
│   └── kustomization.yaml             # Base kustomization that includes all resources
└── environments/                      # Environment-specific overrides
    ├── dev/
    │   └── kustomization.yaml         # Dev environment (image tags, replicas)
    ├── staging/
    │   └── kustomization.yaml         # Staging environment
    └── prod/
        └── kustomization.yaml         # Prod environment (higher replicas)
```

## Quick Start

### Prerequisites

- `kubectl` configured to access your cluster
- `kustomize` installed (or use `kubectl kustomize`)

### Deploy to Dev Environment

```bash
# Preview the manifest
kustomize build kubernetes/environments/dev

# Apply to dev cluster
kubectl apply -k kubernetes/environments/dev/

# Verify deployment
kubectl get pods -n ridebooking
kubectl get svc -n ridebooking
```

### Deploy to Prod Environment

```bash
# Preview the manifest
kustomize build kubernetes/environments/prod

# Apply to prod cluster
kubectl apply -k kubernetes/environments/prod/

# Verify deployment
kubectl get pods -n ridebooking
kubectl get svc -n ridebooking
```

## Configuration Details

### Base Configuration

The `base/kustomization.yaml` includes:
- **Infrastructure**: namespace (`ridebooking`), service account
- **Database**: MySQL deployment with PVC for persistent storage
- **Services**: Auth, Ride, Payment, and Notification microservices

### Environment-Specific Overrides

Each environment directory (`dev`, `staging`, `prod`) uses Kustomize to override:
- **Image tags**: e.g., `auth-service:dev`, `auth-service:prod`
- **Replicas**: Dev=1, Staging=1, Prod=2+ replicas for HA

### Secrets Management

Dev uses generated Kubernetes Secrets from `kubernetes/environments/dev/kustomization.yaml`.

Staging/prod use AWS Secrets Manager through External Secrets Operator:

- Terraform creates `${environment}/ridebooking/db`
- Terraform creates `${environment}/ridebooking/app`
- `SecretStore` authenticates to AWS using IRSA and `ridebooking-sa`
- `ExternalSecret` creates Kubernetes `db-secret` and `app-runtime-secret`

Install External Secrets Operator before applying staging/prod app manifests:

```bash
helm repo add external-secrets https://charts.external-secrets.io
helm repo update
helm install external-secrets external-secrets/external-secrets \
  -n external-secrets \
  --create-namespace \
  --set installCRDs=true
```

Pass secret values to Terraform without committing them:

```bash
terraform -chdir=terraform/environments/prod apply \
  -var="db_password=<strong-db-password>" \
  -var="jwt_secret=<base64-jwt-secret>" \
  -var="admin_password=<strong-admin-password>"
```

### ConfigMaps

Each microservice has a `*-service-config` ConfigMap containing:
- `DB_HOST`: Kubernetes service DNS (e.g., `mysql`)
- `DB_PORT`: 3306
- `DB_USERNAME`: root
- `SPRING_JPA_*`: JPA/Hibernate settings

### Persistent Storage

MySQL uses a PVC (`mysql-pvc`) with 5Gi storage. For production:
- Consider using a managed database (RDS, Cloud SQL) instead
- Or use a StorageClass with backup policies

## Resource Requests/Limits

Each service is configured with:
- **Requests**: 250m CPU, 512Mi memory (guaranteed)
- **Limits**: 500m CPU, 1Gi memory (max)

Adjust these based on your workload testing.

## Image Registry

Currently, image names use local registry placeholders. Update them:

```bash
# For Docker Hub
- name: auth-service
  newName: your-dockerhub-username/auth-service
  newTag: v1.0

# For private registry
- name: auth-service
  newName: registry.example.com/auth-service
  newTag: v1.0
```

Update the `image:` fields in `base/services/*/deployment.yaml`.

## Networking

- Services are exposed via `ClusterIP` (internal only)
- Staging/prod include an AWS ALB `Ingress` from `base/ingress`
- Terraform installs the AWS Load Balancer Controller for staging/prod

Before applying staging/prod manifests, replace placeholders in the overlay with Terraform outputs:

```bash
terraform -chdir=terraform/environments/prod output
```

Use:
- `irsa_service_account_role_arn` in `patches/aws-serviceaccount-patch.yaml`
- `db_endpoint` host part in the RDS ConfigMap patch
- `ecr_repository_urls` in `images[].newName`
- `payment_queue_url` and `notification_queue_url` in `app-runtime-secret`

## Troubleshooting

### Check pod logs

```bash
kubectl logs -n ridebooking deployment/auth-service
kubectl logs -n ridebooking deployment/ride-service
```

### Check service connectivity

```bash
kubectl exec -it -n ridebooking pod/ride-service-xxx -- /bin/sh
# Inside the pod:
curl http://mysql:3306
curl http://auth-service:8081/health
```

### Delete and redeploy

```bash
kubectl delete -k kubernetes/environments/dev/
kubectl apply -k kubernetes/environments/dev/
```

## Future Enhancements

- Add `HorizontalPodAutoscaler` for auto-scaling based on CPU/memory
- Integrate with External Secrets Operator for secure secret management
- Add `NetworkPolicy` for network segmentation
- Add `PodDisruptionBudget` for HA
- Setup `cert-manager` for TLS/SSL certificates
- Configure monitoring with Prometheus & Grafana

## References

- [Kustomize Documentation](https://kustomize.io/)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)
