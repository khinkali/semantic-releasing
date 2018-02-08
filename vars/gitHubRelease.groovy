#!/usr/bin/env groovy

def call(String version, String userName, String repository, String accessToken) {
    def RELEASE_NOTES = "# Release notes for ${version}\\n"
    for (def commitMessage : commitComments(userName, repository)) {
        RELEASE_NOTES += "* ${commitMessage}\\n"
    }
    sh "curl --data '{\"tag_name\": \"${version}\",\"target_commitish\": \"master\",\"name\": \"${version}\",\"body\": \"${RELEASE_NOTES}\",\"draft\": false,\"prerelease\": false}' https://api.github.com/repos/khinkali/sink/releases?access_token=${accessToken}"
}
