# Caylent :: Liferay - CI Pipeline 


## Work Environment
- **Branch**: `master-ci-6005-postgresql`  
- **Folder**: `ci-mvp-downstream-builds/Jenkinsfile`  
- **Repository**: [pablo-inchausti-caylent/liferay-portal](https://github.com/pablo-inchausti-caylent/liferay-portal)  
  _(Forked from [michaelhashimoto/liferay-portal](https://github.com/michaelhashimoto/liferay-portal))_

  
## Features

- Top-Level and Downstream Builds using Jenkins on EKS
- Karpenter-enabled dynamic autoscaling
- Integrated with:
  - **SCM**: `liferay-portal` branch `master-ci-6005-postgresql`
  - **EFS**: Elastic File System for artifacts and repo bundles (`*.tar.gz`)
  - **ECR**: Elastic Container Registry for Docker-based runners
  - **S3**: Stores test results
  - **Custom Shared Libraries** for Jenkins pipelines
    

## Folder Structure

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

## Branches


- `master-ci-6005-postgresql` – Top-level build
- `master-ci-6005-downstream-builds` – Downstream builds

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


