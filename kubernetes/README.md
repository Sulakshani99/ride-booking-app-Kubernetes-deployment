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

Database credentials are stored in `db-secret` (created separately):

```bash
kubectl create secret generic db-secret \
  --from-literal=MYSQL_ROOT_PASSWORD=your-password \
  -n ridebooking
```

Alternatively, use an external secrets operator (e.g., External Secrets, Sealed Secrets) for secure secret management.

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
- For external access, add an `Ingress` in `base/infrastructure/ingress.yaml`
- Or use `NodePort`/`LoadBalancer` service types

Example Ingress (optional):

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ridebooking-ingress
  namespace: ridebooking
spec:
  rules:
    - host: api.example.com
      http:
        paths:
          - path: /auth
            pathType: Prefix
            backend:
              service:
                name: auth-service
                port:
                  number: 8081
```

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
