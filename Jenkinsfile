def cluster_dtls = [
    master: 'eks-ue1-master-primary',
    orange: 'eks-ue1-dev-primary',
    apple: 'eks-ue1-dev-primary',
    blue: 'eks-ue1-dev-primary',
    perf: 'eks-ue1-dev-primary',
    hotfix: 'eks-ue1-dev-primary',
    int0: 'eks-ue1-dev-primary',
    int1: 'eks-ue1-dev-primary',
    int2: 'eks-ue1-dev-primary', 
    int3: 'eks-ue1-dev-primary', 
    int4: 'eks-ue1-dev-primary',
    int5: 'eks-ue1-dev-primary',
    int6: 'eks-ue1-dev-primary',
    int7: 'eks-ue1-dev-primary',
    int8: 'eks-ue1-dev-primary',
    int9: 'eks-ue1-dev-primary',
    inta: 'eks-ue1-dev-primary',
    prod: 'eks-ue1-prod-primary',
    staging: 'eks-ue1-staging-primary',
    demo:'eks-ue1-demo-primary',
    training: 'eks-ue1-training-primary',
]
pipeline {
    agent {
            ecs {
            inheritFrom 'build-slave-helm'
        }
    }
    environment {
        CHART_REPO="https://code.dudesoln.com/projects/CMC/repos/eks-helm-charts/raw/"
    }
    parameters {
        string(name: 'APP_NAME', defaultValue: 'api', description: 'enter the name of application to be deployed')
        choice(name: 'ENVIRONMENT', choices: "${cluster_dtls.keySet().join('\n')}")
        string(name: 'IMAGE_TAG', defaultValue: '1.0.1', description: 'enter the latest image tag')
    }
    stages {
        stage('checkout code from bitbucket') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'BitbucketUser', url: 'https://code.dudesoln.com/scm/cmc/ae-citizenportalweb.git']]]) 
            }
        }
        stage('Deploy helm charts'){
            steps {
                withCredentials([usernamePassword(credentialsId: 'BitbucketUser', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
                    sh '''
                    echo "Building helm chart for application ${APP_NAME}"
                    helm repo add bitbucket ${CHART_REPO} --username=$USERNAME --password=${PASSWORD}
                    helm repo update
                    helm repo ls
                    '''
                }
                withAWS(roleAccount:'750920731536', role:'sharedservices-crossaccount-role'){
                    script {
                        NAMESPACE = env.JOB_BASE_NAME.split('-')[0].concat('-'+params.ENVIRONMENT)
                        sh """
                        aws eks update-kubeconfig --name ${cluster_dtls[params.ENVIRONMENT]} --region us-east-1
                        helm ls
                        helm secrets upgrade --install ${APP_NAME} bitbucket/${APP_NAME} -n ${NAMESPACE} -f helm_vars/${ENVIRONMENT}/configmap.yaml -f helm_vars/${ENVIRONMENT}/secrets.yaml --set image.application.tag="${IMAGE_TAG}"
                        """
                    }
                }
            }
        }
    }
}