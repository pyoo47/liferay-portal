# Caylent :: Liferay Upstream Builds

    Working on branch:
    master-ci-6005-postgresql

    ci-mvp-downstream-builds/Jenkinsfile

# Liferay-eks-mvp

- Upstream Builds 

    with real integration integration to

    - SCM lifetay-portal (this repo) - branch master-ci-6005-postgresql

    - EFS - Elasti File System
    - ECR - Elastic Container Registry docker runners
    - Downstream  Pipelines
    
    - S3 copy
    
# Folder structure

liferay-portal/
└── ci-mvp-downstream-builds/
    ├── downstream-build-1/
    │   ├── Jenkinsfile
    │   └── down-build-1.sh
    ├── downstream-build-2/
    │   ├── Jenkinsfile
    │   └── down-build-2.sh
    ├── downstream-build-3/
    │   ├── Jenkinsfile
    │   └── down-build-3.sh
    ├── downstream-build-4/
    │   ├── Jenkinsfile
    │   └── down-build-4.sh
    ├── downstream-build-5/
    │   ├── Jenkinsfile
    │   └── down-build-5.sh
    └── top-level-build/
        ├── Jenkinsfile
        └── top-level-build.sh
    --- Jenkinsfile      (possible dispatch jenkins file)

# Branchs

    - master-ci-6005-postgresql         -  for top level build
    - master-ci-6005-downstream-builds  -  for top downstream-builds

# Last Pod Build


    POD=$(kubectl get pods -n liferay-jenkins \
    --selector=jenkins=agent \
    --sort-by=.metadata.creationTimestamp \
    -o jsonpath='{.items[-1:].metadata.name}')
    echo $POD
    kubectl exec -it "$POD" -n liferay-jenkins -- /bin/bash

    # Top 20 files in size
    alias duse='du -sh .[!.]* * | sort -hr | head -n 20 '
    alias ll='ls -alh '

# Commnads inside the pod

    DIR_LOCAL_CHECKOUT  = "/home/jenkins/local/checkout"
    EFS_WORKSPACE_DIR   = "/home/jenkins/agent/workspace/${env.JOB_NAME}-${env.BUILD_NUMBER}"
    FILE_TARBALL_NAME   = "liferay-portalrepo-${env.BUILD_NUMBER}.tar.gz"



     kubectl exec -it                      liferay-agent-test-6vljz-sbjk7 -c runner -n liferay-jenkins -- bash
exec kubectl exec -i -t -n liferay-jenkins liferay-agent-test-6vljz-sbjk7 -c aws-caylent-runner -- sh -c "clear; (bash || ash || sh)"
