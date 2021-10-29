def cluster_dtls = [
    master: [ cluster: 'eks-ue1-master-primary', acc_no: '704173473619'],
    orange: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'],
    apple: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'],
    blue: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'],
    perf: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'],
    hotfix: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'],
    int0: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'],
    int1: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'],
    int2: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'], 
    int3: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'],
    int4: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'],
    int5: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'],
    int6: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'],
    int7: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'],
    int8: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'],
    int9: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'],
    inta: [ cluster: 'eks-ue1-dev-primary', acc_no: '704173473619'],
    prod: [ cluster: 'eks-ue1-prod-primary', acc_no: '695288531576'],
    staging: [ cluster: 'eks-ue1-staging-primary', acc_no: '695288531576'],
    demo: [ cluster: 'eks-ue1-demo-primary', acc_no: '695288531576'],
    training: [ cluster: 'eks-ue1-training-primary', acc_no: '695288531576'],
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
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'BitbucketUser', url: 'https://code.dudesoln.com/scm/cmc/${JOB_BASE_NAME}.git']]]) 
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