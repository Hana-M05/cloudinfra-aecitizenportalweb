def cluster_dtls = [
    master: [ cluster: 'eks-ue1-master', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    orange: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    apple: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    blue: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    perf: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    //hotfix: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    //int0: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    int1: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    int2: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    int3: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    int4: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    int5: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    int6: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    int7: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    int8: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    //int9: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    //inta: [ cluster: 'eks-ue1-dev', acc_no: '704173473619', region: 'us-east-1', sns: 'jenkinsnotify-ue1-dev-primary'],
    prod: [ cluster: 'eks-ue1-prod', acc_no: '695288531576', region: 'us-east-1', sns: 'jenkinsnotify-ue1-prod-primary'],
    staging: [ cluster: 'eks-ue1-staging', acc_no: '695288531576', region: 'us-east-1', sns: 'jenkinsnotify-ue1-prod-primary'],
    demo: [ cluster: 'eks-ue1-demo', acc_no: '695288531576', region: 'us-east-1', sns: 'jenkinsnotify-ue1-prod-primary'],
    training: [ cluster: 'eks-ue1-training', acc_no: '695288531576', region: 'us-east-1', sns: 'jenkinsnotify-ue1-prod-primary'],
]
def getEnvList() {
    if("https://jenkins-lzprod.dudesoln.com/".contains("lzprod")){
        return ["staging","prod","demo","training"]
    }
    else {
        return ["master","apple","orange","blue","perf","int1","int2","int3","int4","int5","int6","int7","int8"]
    }
    
}
def ENV_LIST = getEnvList().join('\n')
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
        choice(name: 'ENVIRONMENT', choices: ENV_LIST)
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
                            helm secrets upgrade --install ${JOB_BASE_NAME} bitbucket/${JOB_BASE_NAME} -n ${NAMESPACE} -f helm_vars/${params.ENVIRONMENT}/configmap.yaml -f helm_vars/${params.ENVIRONMENT}/secrets.yaml --set image.application.tag=${params.IMAGE_TAG} --set targetGroupBinding.arn=${TG_ARN} > output.txt 2>&1
                            """
                        }
                    }
                }
            }
        }
    }
    post {
        success {
            echo 'The job has been completed successfully'
            sh 'cat output.txt'
        }
        failure {
            script {
                sh 'cat output.txt'
              	TOPIC_NAME = cluster_dtls[params.ENVIRONMENT].sns
              	println TOPIC_NAME
                env.FILENAME = readFile 'output.txt'
                snsPublish(
                    topicArn: "arn:aws:sns:${REGION}:${ACC_NO}:${TOPIC_NAME}",
                    subject: 'Build failed!['+env.BUILD_URL+']',
                    message: env.FILENAME,
                )
            }
        }
    }
}
