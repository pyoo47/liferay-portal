✦ I have analyzed consoleText4.log and identified a ClassCastException in TestClassGroupFactory._newBatchTestClassGroup as the root cause of the build failure. This occurred because TestBatchFactory.newTestBatch returned a DefaultTestBatch for the unit_stable batch, which was then incorrectly cast to JUnitTestBatch.

  Key Findings & Fixes
   * TestBatchFactory Fix: Updated to recognize more batch name prefixes (e.g., unit, integration, junit-test) for JUnitTestBatch and PoshiTestBatch.
   * JUnitTestSelector Fix: Added support for the test.batch.class.names.includes.required property, which is used by top-level unit tests, alongside the existing modules-specific properties.
   * TestClassGroupFactory Fix: Added instanceof checks before casting testBatch to JUnitTestBatch, PoshiTestBatch, or PlaywrightTestBatch to prevent future crashes.
   * Verification: Verified the changes with a new unit test, TestBatchFactoryTest.java, confirming that unit_stable and other prefixes now correctly return the expected JUnitTestBatch type.

  Referenced Files:
   - file:///Users/pyoo/dev/liferay-portal/master/modules/test/jenkins-results-parser/src/main/java/com/liferay/jenkins/results/parser/test/batch/TestBatchFactory.java
   - file:///Users/pyoo/dev/liferay-portal/master/modules/test/jenkins-results-parser/src/main/java/com/liferay/jenkins/results/parser/test/batch/JUnitTestSelector.java
   - file:///Users/pyoo/dev/liferay-portal/master/modules/test/jenkins-results-parser/src/main/java/com/liferay/jenkins/results/parser/test/clazz/group/TestClassGroupFactory.java
   - file:///Users/pyoo/dev/liferay-jenkins-tools-private/jenkins-results-parser-tools/consoleText4.log