#!/usr/bin/env groovy

def call(String userName, String repositoryName) {
    def latestReleaseJson = sh(
            script: "curl https://api.github.com/repos/${userName}/${repositoryName}/releases/latest",
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
        entry = entry.substring(startIndex + 1).trim().replaceAll("\\\\n", "[NL]").replaceAll("\'", "[SINGLE QUOTE]").replaceAll("\"", "[DOUBLE QUOTE]")
        entry = insertIssueLink(entry, userName, repositoryName)
        allComments << entry
    }

    return allComments
}

def insertIssueLink(def entry, def userName, def repositoryName) {
    def startIndex = entry.indexOf('[')
    def endIndex = entry.indexOf(']')
    if (startIndex == -1 || endIndex == -1) {
        return entry
    }
    def issueNumber = entry.substring(startIndex + 1, endIndex).trim().toInteger()
    return entry.substring(0, startIndex) + "[" + entry.substring(startIndex, endIndex + 1) + "](https://github.com/${userName}/${repositoryName}/issues/" + issueNumber + ")" + entry.substring(endIndex + 1)
}
