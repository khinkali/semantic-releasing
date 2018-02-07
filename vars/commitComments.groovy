#!/usr/bin/env groovy

def call() {
    def latestReleaseJson = sh(
            script: 'curl https://api.github.com/repos/khinkali/sink/releases/latest',
            returnStdout: true
    ).trim()
    def data = readJSON text: "${latestReleaseJson}"

    def commitHistoryText = sh(
            script: "git log ${data.tag_name}..HEAD --oneline",
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
