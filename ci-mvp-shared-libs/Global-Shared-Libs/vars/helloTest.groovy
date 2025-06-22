def call() {

    def result = readJSON text: libraryResource('com/liferay/test.json')

    sh "echo ${result.company}"

}

