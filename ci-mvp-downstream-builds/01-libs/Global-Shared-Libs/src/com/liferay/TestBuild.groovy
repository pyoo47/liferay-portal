/**
* Use commit messages to trigger tests individually.
* Examples, using TestBuild enum: 
*
*   - [test: test-01-unit]     → Run test-01-unit
*   - [test: test-02-func]     → Run test-02-func
*   - [test: test-03-db]       → Run test-03-db
*   - [test: test-04-unit]     → Run test-04-unit
*   - [test: test-05-unit]     → Run test-05-unit
*   - [test: ALL]              → Run all tests in parallel
*   - [test: NONE]             → Skip all tests
*
* Combined messages are also supported:
*   - [test: test-01-unit, test-02-func]
*   - [test: test-01-unit, test-02-func, test-03-db, test-04-unit, test-05-unit]
*
* TestBuild enum to manage downstream builds
* echo TestBuild.TEST_02.getFullJobPath()   // MVP-Downstream-Builds/MVP-Downstream-Build-2/master-ci-6005-downstream-builds
* echo TestBuild.TEST_03.getFullDirPath()   // ci-mvp-downstream-builds/downstream-build-3
*/
package com.liferay

class TestBuild {

  static final String BUILD_FOLDER = 'MVP-Downstream-Builds'
  static final String HOME_DIR     = 'ci-mvp-downstream-builds'
  static final String BRANCH       = 'master-ci-6005-downstream-builds'

  static final List<Map<String, String>> TESTS = [
    [label: 'test-01-unit', jobName: 'MVP-Downstream-Build-1', dir: 'downstream-build-1'],
    [label: 'test-02-func', jobName: 'MVP-Downstream-Build-1', dir: 'downstream-build-1'],
    [label: 'test-03-db',   jobName: 'MVP-Downstream-Build-1', dir: 'downstream-build-1'],
    [label: 'test-04-unit', jobName: 'MVP-Downstream-Build-1', dir: 'downstream-build-1'],
    [label: 'test-05-unit', jobName: 'MVP-Downstream-Build-1', dir: 'downstream-build-1']
  ]


  /*  // Example of how to use the TESTS list
  // Uncomment to use in your code
  static final List<Map<String, String>> TESTS = [
    [label: 'test-01-unit', jobName: 'MVP-Downstream-Build-1', dir: 'downstream-build-1'],
    [label: 'test-02-func', jobName: 'MVP-Downstream-Build-2', dir: 'downstream-build-2'],
    [label: 'test-03-db',   jobName: 'MVP-Downstream-Build-3', dir: 'downstream-build-3'],
    [label: 'test-04-unit', jobName: 'MVP-Downstream-Build-4', dir: 'downstream-build-4'],
    [label: 'test-05-unit', jobName: 'MVP-Downstream-Build-5', dir: 'downstream-build-5']
  ]
  */

  static List<Map> getAll() {
    return TESTS
  }

  static List<Map> fromCommitMessage(String commitMsg) {
    def matcher = commitMsg =~ /\[test:\s*(.*?)\]/
    if (!matcher.find()) return getAll()

    def tokens = matcher.group(1).split(',').collect { it.trim().toLowerCase() }

    if (tokens.contains('all')) return getAll()
    if (tokens.contains('none')) return []

    return getAll().findAll { test -> tokens.contains(test.label.toLowerCase()) }
  }

  static String getFullJobPath(Map test) {
    return "${BUILD_FOLDER}/${test.jobName}/${BRANCH}"
  }

  static String getFullDirPath(Map test) {
    return "${HOME_DIR}/${test.dir}"
  }
}
