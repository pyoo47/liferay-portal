// vars/sayHello.groovy
def call(String name = 'human') {
    echo "Hello, ${name}."
}

// // Usage in a Jenkinsfile:
// sayHello 'Joe'
// sayHello() /* invoke with default arguments */

