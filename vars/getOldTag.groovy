#!/usr/bin/env groovy

def call() {
    try {
        return sh(
                script: 'git describe --tags --abbrev=0',
                returnStdout: true
        ).trim()
    } catch (ex) {
        return "0.0.0"
    }
}
