# Caylent :: Liferay - Top Level and Downpstream Builds 

    Working on branch:
    master-ci-6005-postgresql

    ci-mvp-downstream-builds/Jenkinsfile

# Liferay-eks-mvp

- Top Level and Downpstream Builds 

    Jenkins on EKS an Karpenter for Autoscaling

    With integration to

    - SCM - lifetay-portal - branch master-ci-6005-postgresql
    - EFS - Elasti File System
    - ECR - Elastic Container Registry docker runners
    - S3  - Copy test results
    - Downstream Pipelines
    - Custom Shared Libs
    
# Folder structure


```text
liferay-portal/
└── ci-mvp-downstream-builds/
    │
    ├── Jenkinsfile              (Top Level Build)
    │
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
    └── downstream-build-5/
        ├── Jenkinsfile
        └── down-build-5.sh
```

# Branchs

    - master-ci-6005-postgresql         -  for top level build
    - master-ci-6005-downstream-builds  -  for top downstream-builds

# Commnads inside the pod

    LOCAL_DIR_CHECKOUT  = "/opt/dev/projects/github"

    EFS_MOUNT_PATH                = "/home/jenkins/agent"
    EFS_LIFERAY_ARTIFACT_FOLDER   = "${EFS_MOUNT_PATH}/liferay-artifacts/build-${env.CUSTOM_BUILD_NUMBER}"
    EFS_LIFERAY_ARTIFACT_REPOS    = "${EFS_LIFERAY_ARTIFACT_FOLDER}/repo-github"
    EFS_LIFERAY_ARTIFACT_BUNDLES  = "${EFS_LIFERAY_ARTIFACT_FOLDER}/bundles"
    EFS_LIFERAY_ARTIFACT_TEST     = "${EFS_LIFERAY_ARTIFACT_FOLDER}/tests"

    BUILD_BUNDLE_NAME   = 'liferay-portal-bundle-tomcat.tar.gz'
    BUILD_SOURCE_NAME   = 'liferay-portal-source.tar.gz'

    TARBALL_GIT_NAME_PL = "liferay-portal-repo-${env.CUSTOM_BUILD_NUMBER}.tar.gz"
    TARBALL_GIT_NAME_EE = "liferay-jenkins-ee-repo-${env.CUSTOM_BUILD_NUMBER}.tar.gz"

    TARBALL_GIT_REPO_PL = "${LOCAL_DIR_CHECKOUT}/${TARBALL_GIT_NAME_PL}"
    TARBALL_GIT_REPO_EE = "${LOCAL_DIR_CHECKOUT}/${TARBALL_GIT_NAME_EE}"


## AWS Sandbox Tagging - Summary

    caylent:project: Liferay CI Process Modernization
    caylent:workload: EKS Liferay Mvp
    map-migrated: migJJYVOZAD8H


