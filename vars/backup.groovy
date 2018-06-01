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

    def execInContainer = "${kc} exec ${podName} -c ${containerName} --"
    try {
        def backslash = "\\\\"
        sh "${execInContainer} find ${containerPath} -type d -empty -exec touch .gitignore"
    } catch (e) {
        echo e.getMessage()
    }

    def git = "git --git-dir '${containerPath}/.git' --work-tree '${containerPath}'"
    sh "${execInContainer} ${git} config user.email \"${gitEmail}\""
    sh "${execInContainer} ${git} config user.name \"${gitName}\""
    sh "${execInContainer} ${git} add --all ."
    sh "${execInContainer} ${git} diff --quiet && ${execInContainer} ${git} diff --staged --quiet || ${execInContainer} ${git} commit -am '${commitMessage}'"
    withCredentials([usernamePassword(credentialsId: repositoryCredentials, passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
        sh "${execInContainer} ${git} push https://${GIT_USERNAME}:${GIT_PASSWORD}@${repositoryUrl}"
    }
}