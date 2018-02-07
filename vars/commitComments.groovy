#!/usr/bin/env groovy

def call() {
    def commitHistoryText = sh(
            script: 'git log `git describe --tags --abbrev=0`..HEAD --oneline',
            returnStdout: true
    ).trim()

    def allComments = []
    def array = commitHistoryText.split('\n')
    for (def i = 0; i < array.size(); i++) {
        def entry = array[i]
        echo "entry: ${entry}"
        def startIndex = entry.indexOf(']')
        echo "startIndex: ${startIndex}"
        if (startIndex == -1) {
            continue
        }
        allComments << entry.substring(startIndex + 1).trim()
        echo "allComments: ${allComments}"
    }

    return allComments
}
