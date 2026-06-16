def ENV_NAME = getEnvName(env.BRANCH_NAME)
def CONTAINER_NAME = "calculator-" +ENV_NAME
def CONTAINER_TAG = getTag(env.BUILD_NUMBER, env.BRANCH_NAME)
def HTTP_PORT = getHTTPPort(env.BRANCH_NAME)
def EMAIL_RECIPIENTS = "nassiyannjunior@gmail.com"


node {
    try {

        stage('Initialize') {
            def dockerHome = tool 'dockerlatest'
            def mavenHome = tool 'mavenlatest'
            env.PATH = "${dockerHome}/bin:${mavenHome}/bin:${env.PATH}"
        }

        stage('Checkout') {
            checkout scm
        }

        stage('Build with test') {
            sh "mvn clean install"
        }

        stage('Sonarqube Analysis') {
            withSonarQubeEnv('localhost_sonarqube') {
                sh " mvn sonar:sonar -Dintegration-tests.skip=true -Dmaven.test.failure.ignore=true"
            }

            timeout(time: 1, unit: 'MINUTES') {
                def qg = waitForQualityGate()
                if (qg.status != 'OK') {
                    error "Pipeline aborted due to quality gate failure: ${qg.status}"
                }
            }
        }

        stage("Image Prune") {
            imagePrune(CONTAINER_NAME)
        }

        stage('Image Build') {
            withCredentials([usernamePassword(credentialsId: 'dockerhubcredentials',
                                              usernameVariable: 'USERNAME',
                                              passwordVariable: 'PASSWORD')]) {

                sh "docker build -t $USERNAME/$CONTAINER_NAME:$CONTAINER_TAG ."
            }
        }

        stage('Push to Docker Registry') {
            withCredentials([usernamePassword(credentialsId: 'dockerhubcredentials',
                                              usernameVariable: 'USERNAME',
                                              passwordVariable: 'PASSWORD')]) {

                sh '''
                    echo "$PASSWORD" | docker login -u "$USERNAME" --password-stdin
                '''

                sh '''
                    docker push $USERNAME/''' + CONTAINER_NAME + ':' + CONTAINER_TAG + '''
                '''
            }
        }
        stage('Run App') {
            withCredentials([usernamePassword(credentialsId: 'dockerhubcredentials',
                                              usernameVariable: 'USERNAME',
                                              passwordVariable: 'PASSWORD')]) {

                runApp(CONTAINER_NAME, CONTAINER_TAG, USERNAME, HTTP_PORT, ENV_NAME)
            }
        }
    } finally {
        deleteDir()
        sendEmail(EMAIL_RECIPIENTS)
    }
}



def imagePrune(containerName) {
    try {
            sh "docker image prune -f"
            sh "docker stop $containerName"
    } catch (ignored) {
    }
}


def imageBuild(containerName, tag) {
    def image = "${containerName}:${tag}"
    sh "docker build -t $image --pull --no-cache ."
}

def pushToImage(containerName, tag, dockerUser, dockerPassword) {

    def image = "${dockerUser}/${containerName}:${tag}"

    sh """
        echo "$dockerPassword" | docker login -u "$dockerUser" --password-stdin
    """

    sh """
        docker push $image
    """

    echo "Image push complete: $image"
}

def runApp(containerName, tag, dockerHubUser, httpPort, envName) {

     def image = "${dockerHubUser}/${containerName}:${tag}"

     sh "docker pull $image"

     sh """
         docker rm -f $containerName || true

         docker run -d \
         --name $containerName \
         -p $httpPort:8080 \
         -e SPRING_PROFILES_ACTIVE=$envName \
         $image
     """

     echo "Application started: $image on port $httpPort"
 }


def sendEmail(recipients) {
    mail(
            to: recipients,
            subject: "Build ${env.BUILD_NUMBER} - ${currentBuild.currentResult} - (${currentBuild.fullDisplayName})",
            body: "Check console output at: ${env.BUILD_URL}/console" + "\n")
}

String getEnvName(String branchName) {
    if (branchName == 'main') {
        return 'prod'
    }
    return (branchName == 'develop') ? 'uat' : 'dev'
}

String getHTTPPort(String branchName) {
    if (branchName == 'main') {
        return '9003'
    }
    return (branchName == 'develop') ? '9002' : '9001'
}

String getTag(String buildNumber, String branchName) {
    if (branchName == 'main') {
        return buildNumber + '-unstable'
    }
    return buildNumber + '-stable'
}
