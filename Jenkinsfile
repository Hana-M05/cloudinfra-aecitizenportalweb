def cluster_dtls = [
    master: [ cluster: 'eks-ue1-master-primary', acc_no: '750920731536'],
    orange: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'],
    apple: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'],
    blue: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'],
    perf: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'],
    hotfix: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'],
    int0: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'],
    int1: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'],
    int2: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'], 
    int3: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'],
    int4: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'],
    int5: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'],
    int6: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'],
    int7: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'],
    int8: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'],
    int9: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'],
    inta: [ cluster: 'eks-ue1-dev-primary', acc_no: '12345678900'],
    prod: [ cluster: 'eks-ue1-prod-primary', acc_no: '12345678900'],
    staging: [ cluster: 'eks-ue1-staging-primary', acc_no: '12345678900'],
    demo: [ cluster: 'eks-ue1-demo-primary', acc_no: '12345678900'],
    training: [ cluster: 'eks-ue1-training-primary', acc_no: '12345678900'],
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
        //string(name: 'APP_NAME', defaultValue: 'api', description: 'enter the name of application to be deployed')
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
                    sh """
                    echo "Building helm chart for application ${JOB_BASE_NAME}"
                    helm repo add bitbucket ${CHART_REPO} --username=$USERNAME --password=${PASSWORD}
                    helm repo update
                    helm repo ls
                    """
                }
                script {
                    acc_no = cluster_dtls[params.ENVIRONMENT].acc_no
                    withAWS(roleAccount: acc_no, role:'sharedservices-crossaccount-role'){
                        NAMESPACE = env.JOB_BASE_NAME.split('-')[0].concat('-'+params.ENVIRONMENT)
                        sh """
                        aws eks update-kubeconfig --name ${cluster_dtls[params.ENVIRONMENT].cluster} --region us-east-1
                        helm ls
                        helm secrets upgrade --install ${JOB_BASE_NAME} bitbucket/${JOB_BASE_NAME} -n ${NAMESPACE} -f helm_vars/${ENVIRONMENT}/configmap.yaml -f helm_vars/${ENVIRONMENT}/secrets.yaml --set image.application.tag="${IMAGE_TAG}"
                        """
                    }
                }
            }
        }
    }
}