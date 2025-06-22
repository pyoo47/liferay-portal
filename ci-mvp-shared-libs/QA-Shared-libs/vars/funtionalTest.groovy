// vars/funtionalTest.groovy
def call(String name = 'human') {
    echo "Hello, ${name}."
}

// // Usage in a Jenkinsfile:
// funtionalTest 'Joe'
// funtionalTest() /* invoke with default arguments */

