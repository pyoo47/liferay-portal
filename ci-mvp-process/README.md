# Caylent :: Liferay 

    Working on branch:
    master-ci-6005-postgresq

    ci-mvp-process/SampleApp_01/Jenkinsfile

# Liferay-eks-mvp

## AWS Sandbox Tagging - Liferay

Tag the workloads to get the credits from Liferay MAP program:

- Tag: map-migrated
- Value: migJJYVOZAD8H

Tag infrastructure with caylent:customer, if this infrastructure is for customer testing

- caylent:owner     : (required) - caylent.com email
- caylent:customer  :  Liferay, Inc
- caylent:project   :  Liferay CI Process Modernization

About the MVP:

- caylent:workload  :  EKS Liferay Mvp
- caylent:workload  :  EKS Liferay Config
- caylent:workload  :  Jenkins on EKS
- caylent:workload  :  Github Actions with EKS


## AWS Sandbox Tagging - Summary

master-ci-6005-postgresq

    caylent:owner: pablo.inchausti@caylent.com
    caylent:project: Liferay CI Process Modernization
    caylent:workload: EKS Liferay Mvp
    map-migrated: migJJYVOZAD8H

## Recomendation

- Switch to us-east-2 (Ohio) region, as it is consistently cheaper than us-east-1 for many spot instance types.
