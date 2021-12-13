# To Deploy Helm Charts
## Prerequisites
- helm has to be installed. Refer the link https://helm.sh/docs/intro/install/
- helm secrets has to be installed. Refer the link https://developer.epages.com/blog/tech-stories/kubernetes-deployments-with-helm-secrets/
## Steps to run helm charts
1. Add the bitbucket repo
> helm repo add bitbucket [https://code.dudesoln.com/projects/CMC/repos/eks-helm-charts/raw/](https://code.dudesoln.com/projects/CMC/repos/eks-helm-charts/raw/) --username=**username** --password=**password**
3. Clone this repo in your local
4.  Run the below command inside the cloned repo.
 > helm secrets upgrade --install 
 -f helm_vars/env/secrets.yaml 
-f helm_vars/env/configmap.yaml 
--set image.application.tag=version 
--set targetGroupBinding.arn=targetgrouparn
-n namespace

## Steps to update secrets
1. To update secrets in lower environment, copy paste the AWS Temporary credentials.
2. Run the below command to decrypt the secrets stored in bitbucket
> helm secrets edit helm_vars/environment/secrets.yaml
3. Push the updated code to bitbucket.

## Steps to update the helm chart
1. Run the below command outside the **src/** folder
> helm package src/
> mv repo-name.tar.gz ../eks_helm_charts/dude-charts/
> cd ../eks-helm-charts/
> helm repo index .
2. Push the changes in application repo and helm chart repo.
