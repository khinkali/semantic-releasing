#!/usr/bin/env groovy

def call(String podLabel,
         String containerName,
         String containerPath,
         String repositoryUrl,
         String kc = 'kubectl',
         String gitEmail = 'jenkins@khinkali.ch',
         String gitName = 'Jenkins',
         String commitMessage = 'new_version',
         String repositoryCredentials = 'bitbucket') {
    def jenkinsPods = sh(
            script: "${kc} get po -l ${podLabel} --no-headers",
            returnStdout: true
    ).trim()
    def podNameLine = jenkinsPods.split('\n')[0]
    def startIndex = podNameLine.indexOf(' ')
    if (startIndex == -1) {
        return
    }
    def podName = podNameLine.substring(0, startIndex)
    def git = "git --git-dir '${containerPath}/.git' --work-tree '${containerPath}'"
    sh "${kc} exec ${podName} -c ${containerName} -- ${git} config user.email \"${gitEmail}\""
    sh "${kc} exec ${podName} -c ${containerName} -- ${git} config user.name \"${gitName}\""
    sh "${kc} exec ${podName} -c ${containerName} -- ${git} add --all"
    sh "${kc} exec ${podName} -c ${containerName} -- ${git} diff --quiet && ${kc} exec ${podName} -c ${containerName} -- ${git} diff --staged --quiet || ${kc} exec ${podName} -c ${containerName} -- ${git} commit -am '${commitMessage}'"
    withCredentials([usernamePassword(credentialsId: repositoryCredentials, passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
        sh "${kc} exec ${podName} -c ${containerName} -- ${git} push https://${GIT_USERNAME}:${GIT_PASSWORD}@${repositoryUrl}"
    }
}