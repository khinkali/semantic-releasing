#!/usr/bin/env groovy

def call(String name) {
    cleanWs()
    stage('checkout') {
        git url: "https://github.com/khinkali/${name}"
    }

    stage('build image & git tag & docker push') {
        env.VERSION = semanticReleasing()
        currentBuild.displayName = env.VERSION

        sh "git config user.email \"jenkins@khinkali.ch\""
        sh "git config user.name \"Jenkins\""
        sh "git tag -a ${env.VERSION} -m \"${env.VERSION}\""
        withCredentials([usernamePassword(credentialsId: 'github', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
            sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/khinkali/${name}.git --tags"
        }

        container('docker') {
            sh "docker build -t khinkali/${name}:${env.VERSION} ."
            withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                sh "docker login --username ${DOCKER_USERNAME} --password ${DOCKER_PASSWORD}"
            }
            sh "docker push khinkali/${name}:${env.VERSION}"
        }
    }

    stage('deploy') {
        sh "sed -i -e 's/        image: khinkali\\/${name}:todo/        image: khinkali\\/${name}:${env.VERSION}/' kubeconfig.yml"
        container('kubectl') {
            sh "kubectl apply -f kubeconfig.yml"
        }
    }
}