package com.liferay.qa

class FunctionalTest implements Serializable {
  def steps
  FunctionalTest(steps) {this.steps = steps}
  def mvn(args) {
    steps.sh "${steps.tool 'Maven'}/bin/mvn -o ${args}"
  }
}