// vars/log.groovy
def info(message) {
    echo "INFO: ${message}"
}

def warning(message) {
    echo "WARNING: ${message}"
}


// Jenkinsfile
// @Library('Liferay-QA-Shared-Libs') _

// log.info 'Starting'
// log.warning 'Nothing to do!'