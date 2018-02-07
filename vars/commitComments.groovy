#!/usr/bin/env groovy

def call() {
    def latestReleaseJson = sh(
            script: 'curl https://api.github.com/repos/khinkali/sink/releases/latest',
            returnStdout: true
    ).trim()
    def data = readJSON text: "${latestReleaseJson}"
    echo "tag: ${data.tag_name}"

    def commitHistoryText = sh(
            script: 'git log `git describe --tags --abbrev=0`..HEAD --oneline',
            returnStdout: true
    ).trim()

    def allComments = []
    def array = commitHistoryText.split('\n')
    for (def i = 0; i < array.size(); i++) {
        def entry = array[i]
        def startIndex = entry.indexOf(']')
        if (startIndex == -1) {
            continue
        }
        allComments << entry.substring(startIndex + 1).trim()
    }

    return allComments
}
