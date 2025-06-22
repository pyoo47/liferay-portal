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

# Branchs

    - master-ci-6005-postgresql         -  for top level build
    - master-ci-6005-downstream-builds  -  for top downstream-builds