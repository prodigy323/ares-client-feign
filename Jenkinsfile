def mvnTool
def prjName = "ares-client-feign"
def imageTag = "latest"

pipeline {
    agent { label 'maven' }
    options {
        buildDiscarder(logRotator(numToKeepStr: '2'))
        disableConcurrentBuilds()
    }
    stages {
        stage('Build && Test') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    script {
                        mvnTool = tool 'Maven'
                        sh "${mvnTool}/bin/mvn -B clean verify sonar:sonar -Prun-its,coverage"
                    }
                }
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco(execPattern: 'target/jacoco.exec')
                }
            }
        }
        stage('Quality Gate') {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Release && Publish Artifact') {

        }
        stage('Create Image') {
            steps {
                sh "docker build --build-arg JAR_FILE=target/${prjName}-${releaseVersion}.jar -t ${prjName}:${releaseVersion}"
            }
        }
        stage('Publish Image') {
            steps {
                withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'JENKINS_ID', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                    sh """
                        docker login -u ${USERNAME} -p ${PASSWORD} dockerRepoUrl
                        docker push ...
                    """
                }
            }
        }
    }
}