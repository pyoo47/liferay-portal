// vars/log.groovy
def info(message) {
    echo "INFO: ${message}"
}

def warning(message) {
    echo "WARNING: ${message}"
}


// Jenkinsfile
// @Library('utils') _

// log.info 'Starting'
// log.warning 'Nothing to do!'