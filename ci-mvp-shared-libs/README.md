# Shared libs for Jenkins

In theory - Shared Libs should have their own repositroy

# Resources

name: Liferay-Shared-Libs
https://github.com/caylent/Liferay-eks-mvp.git

Default version
main

Library Path (optional)
Shared-Libs

# Resources

Following the docs
https://docs.cloudbees.com/docs/cloudbees-ci-kb/latest/client-and-managed-controllers/how-to-build-monorepos-in-cloudbees-ci
https://www.jenkins.io/blog/2017/10/02/pipeline-templates-with-shared-libraries/

https://www.jenkins.io/doc/book/pipeline/shared-libraries/


# Structure
https://www.jenkins.io/doc/book/pipeline/shared-libraries/

(root)
+- src                     # Groovy source files
|   +- org
|       +- foo
|           +- Bar.groovy  # for org.foo.Bar class
+- vars
|   +- foo.groovy          # for global 'foo' variable
|   +- foo.txt             # help for 'foo' variable
+- resources               # resource files (external libraries only)
|   +- org
|       +- foo
|           +- bar.json    # static helper data for org.foo.Bar