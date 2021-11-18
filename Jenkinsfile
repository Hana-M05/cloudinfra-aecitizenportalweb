def cluster_dtls = [
    master: [ cluster: 'eks-ue1-master', acc_no: '704173473619', region: 'us-east-1'],
    orange: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'],
    apple: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'],
    blue: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'],
    perf: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'],
    hotfix: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'],
    int0: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'],
    int1: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'],
    int2: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'], 
    int3: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'],
    int4: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'],
    int5: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'],
    int6: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'],
    int7: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'],
    int8: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'],
    int9: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'],
    inta: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1'],
    prod: [ cluster: 'eks-ue1-prod', acc_no: '695288531576', region: 'us-east-1'],
    staging: [ cluster: 'eks-ue1-staging', acc_no: '695288531576', region: 'us-east-1'],
    demo: [ cluster: 'eks-ue1-demo', acc_no: '695288531576', region: 'us-east-1'],
    training: [ cluster: 'eks-ue1-training', acc_no: '695288531576', region: 'us-east-1'],
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
        // stage('checkout code from bitbucket') {
        //     steps {
        //         checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'BitbucketUser', url: 'https://code.dudesoln.com/scm/cmc/${JOB_BASE_NAME}.git']]]) 
        //     }
        // }
        stage('Get LB Details'){
            steps {
                script {
                    ACC_NO      = cluster_dtls[params.ENVIRONMENT].acc_no
                    CLUSTER_NAME = cluster_dtls[params.ENVIRONMENT].cluster
                    REGION      = cluster_dtls[params.ENVIRONMENT].region
                    REGION_SHORT   = CLUSTER_NAME.split('-')[1]
                    ALB_NAME     = 'cpwebalb-'.concat(REGION_SHORT+'-'+params.ENVIRONMENT+'-primary')
                    withAWS(roleAccount: ACC_NO, role:'sharedservices-crossaccount-role'){                        
                        sh "aws eks update-kubeconfig --name ${CLUSTER_NAME} --region ${REGION}"
                        ALB_ARN = sh(returnStdout: true, script: "aws elbv2 describe-load-balancers --region ${REGION} | grep LoadBalancerArn | grep ${ALB_NAME} | cut -d '\"' -f4").trim()
                        println ALB_ARN
                        TG_ARN = sh(returnStdout: true, script: "aws elbv2 describe-target-groups --load-balancer-arn ${ALB_ARN} | grep TargetGroupArn | cut -d '\"' -f4").trim()
                        println TG_ARN
                    }
                }
            }
        }
        stage('Deploy Helm Charts'){
            steps {
                withCredentials([usernamePassword(credentialsId: 'BitbucketUser', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
                    sh 'echo Building helm chart for application ${JOB_BASE_NAME}'
                    sh 'helm repo add bitbucket ${CHART_REPO} --username=$USERNAME --password=${PASSWORD}'
                    sh 'helm search repo bitbucket'
                    sh 'helm repo update'
                    script {
                        NAMESPACE   = env.JOB_BASE_NAME.split('-')[0].concat('-'+params.ENVIRONMENT)
                        withAWS(roleAccount: ACC_NO, role:'sharedservices-crossaccount-role'){  
                            sh """
                            helm secrets upgrade --install ${JOB_BASE_NAME} bitbucket/${JOB_BASE_NAME} -n ${NAMESPACE} -f helm_vars/${params.ENVIRONMENT}/configmap.yaml -f helm_vars/${params.ENVIRONMENT}/secrets.yaml --set image.application.tag=${params.IMAGE_TAG} --set targetGroupBinding.arn=${TG_ARN}                    
                            """
                        }
                    }
                }
            }
        }
    }
}