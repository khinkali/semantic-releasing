#!/usr/bin/env groovy

def call(String name, String deploymentFileName = 'kubeconfig.yml', Boolean dockerBuild = true, Boolean tagging = true) {
    cleanWs()
    stage('checkout') {
        git url: "https://github.com/khinkali/${name}"
    }

    if (tagging || dockerBuild) {
        stage('build image & git tag & docker push') {
            if (tagging) {
                env.VERSION = semanticReleasing()
                currentBuild.displayName = env.VERSION

                sh "git config user.email \"jenkins@khinkali.ch\""
                sh "git config user.name \"Jenkins\""
                sh "git tag -a ${env.VERSION} -m \"${env.VERSION}\""
                withCredentials([usernamePassword(credentialsId: 'github', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                    sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/khinkali/${name}.git --tags"
                }
            }

            if (dockerBuild) {
                container('docker') {
                    sh "docker build -t khinkali/${name}:${env.VERSION} ."
                    withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        sh "docker login --username ${DOCKER_USERNAME} --password ${DOCKER_PASSWORD}"
                    }
                    sh "docker push khinkali/${name}:${env.VERSION}"
                }
            }
        }
    }

    stage('deploy') {
        if (dockerBuild) {
            sh "sed -i -e 's/        image: khinkali\\/${name}:todo/        image: khinkali\\/${name}:${env.VERSION}/' ${deploymentFileName}"
        }
        container('kubectl') {
            sh "kubectl apply -f ${deploymentFileName}"
        }
    }
}