#!/usr/bin/env groovy

def call(List<String> majorKeywords = ['API'], List<String> minorKeywords = ['FEAT']) {
    def commitHistoryText = sh(
            script: 'git log `git describe --tags --abbrev=0`..HEAD --oneline',
            returnStdout: true
    ).trim()

    def allMarkers = []
    def array = commitHistoryText.split('\n')
    for (def i = 0; i < array.size(); i++) {
        def entry = array[i]
        def startIndex = entry.indexOf('[')
        def endIndex = entry.indexOf(']')
        if (startIndex == -1 || endIndex == -1) {
            continue
        }
        allMarkers << entry.substring(startIndex + 1, endIndex).trim()
    }

    def oldTag = sh(
            script: 'git describe --tags --abbrev=0',
            returnStdout: true
    ).trim()
    def versionParts = oldTag.split('\\.')
    def major = versionParts[0].toInteger()
    def minor = versionParts[1].toInteger()
    def bug = versionParts[2].toInteger()

    if (!allMarkers.disjoint(majorKeywords)) {
        major += 1
        minor = 0
        bug = 0
    } else if (allMarkers.disjoint(minorKeywords)) {
        minor += 1
        bug = 0
    } else {
        bug += 1
    }

    return "${major}.${minor}.${bug}"
}
