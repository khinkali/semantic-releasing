#!/usr/bin/env groovy

def call(String version, String healthUrl, Integer sleepInSeconds = 1, Integer timeoutInSeconds = 2) {
    def versionText = sh(
            script: "curl ${healthUrl} --max-time ${timeoutInSeconds}",
            returnStdout: true
    ).trim()
    while (versionText != version) {
        sleep sleepInSeconds
        echo "still waiting - version is ${versionText} and should be ${version}"
        versionText = sh(
                script: "curl ${healthUrl} --max-time ${timeoutInSeconds}",
                returnStdout: true
        ).trim()
    }
}
