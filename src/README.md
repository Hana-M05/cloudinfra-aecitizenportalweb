1. To package the charts run the below command
```
helm package ../ae-citizenportalweb/
```
2. Move the created packages to eks-helm-charts repo and recreate index
```
mv ae-citizenportalweb-<version>.tgz <eks-helm-charts repo>/dude-charts/
cd <eks-helm-charts repo>/ && helm repo index .
```
3. Commit the created chart and index.yaml  in eks-helm-charts repo
4. Place the secrets,.sops.yaml and configmap of the corresponding environment in eks-helm-vars repo. Before commiting the values, encrypt the secrets.yaml
```
helm enc <eks-helm-vars repo>/ae-citizenportalweb/<environment>/secrets.yaml
```
