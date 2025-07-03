# Caylent :: Workflow libs

To enable dynamic behavior depending on the commit message

# Shared libs for Jenkins

Recomendation is that Shared Libs have their own repositroy
But, it also support multiple local folder

# Resources

name: Global-Shared-Libs
https://github.com/caylent/Liferay-eks-mvp.git

Default version
main

Library Path (optional)
Global-Shared-Libs

PoC-20-Shared-Libs/Global-Shared-Libs

## Config

   name: Liferay-Global-Shared-Libs
   repo: https://github.com/caylent/Liferay-eks-mvp.git
   branch: main
   credential: Personal Access Token (Pablo Inchusti)
   Library Path: PoC-20-Shared-Libs/Global-Shared-Libs/



# References

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