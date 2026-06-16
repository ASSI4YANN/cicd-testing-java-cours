def ENV_NAME = getEnvName(env.BRANCH_NAME)
def CONTAINER_NAME = "calculator-" + ENV_NAME
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
                sh "mvn sonar:sonar -Dintegration-tests.skip=true -Dmaven.test.failure.ignore=true"
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
            imageBuild(CONTAINER_NAME, CONTAINER_TAG, USERNAME)
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