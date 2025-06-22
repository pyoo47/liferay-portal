def call() {

    def result = readJSON text: libraryResource('com/liferay/qa/qa-sample-values.json')

    sh "echo ${result.name}"
    sh "echo ${result.version}"

}

