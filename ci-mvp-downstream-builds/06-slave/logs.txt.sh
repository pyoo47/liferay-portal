## Bundle Creation Script:  bash script

00 - bundle cretion scripttio 

#
# This script is aimed at creating two large artifacts used in our downstream testing scripts.
#    - liferay-portal-bundle-tomcat.tar.gz  (~1.7gb)
#    - liferay-portal-source.tar.gz         (~1.2gb)
#

	mkdir -p /opt/dev/projects/github

	cd /opt/dev/projects/github


	mkdir -p	/home/jenkins/local/checkout
	cd /home/jenkins/local/checkout


#
# Issue #1 - How do we handle large git repositories?
#
# This portion of the script is cloning a repository that is needed to build a bundle artifact.
#     - liferay-portal     (~15gb)
#     - liferay-jenkins-ee (~250mb)
#

	git clone --depth 1 --no-tags --quiet -b master-ci-6005-postgresql git@github.com:michaelhashimoto/liferay-portal.git
	git clone --depth 1 --no-tags --quiet -b master git@github.com:liferay/liferay-jenkins-ee.git

	Nota: this is inside the ant
	git clone --depth 1     git@github.com:liferay/liferay-portal-ee.git
	git clone --depth 1 https://github.com/liferay/liferay-portal-ee.git
	                    https://github.com/liferay/liferay-portal.git



#
# This portion of the script is setting up the environment to be able to build our bundle artifacts
#
export ANT_OPTS="-Xmx6g"
export GRADLE_OPTS="-Xmx16g"
# top -o %MEM


	cd /opt/dev/projects/github/liferay-portal

	git clean -dfx

	echo "app.server.parent.dir=/opt/dev/projects/github/liferay-portal/bundles" > app.server.${HOSTNAME}.properties

	echo "liferay.home=/opt/dev/projects/github/liferay-portal/bundles" > build.${HOSTNAME}.properties
	echo "mirrors.hostname=" >> build.${HOSTNAME}.properties

	echo "java.jdk.default.runtime.version=jdk21" > test.${HOSTNAME}.properties

	ant setup-profile-dxp

	ant setup-profile-dxp  | tee setup-profile-dxp.log | file - 


#
# This portion of the script actually builds the bundle artifacts
#    - liferay-portal-bundle-tomcat.tar.gz  (~1.7gb)
#    - liferay-portal-source.tar.gz         (~1.2gb)
#

	ant -f build-test-batch.xml dist-jdk7 -Daxis.variable=tomcat


	ant -f build-test-batch.xml dist-jdk7 -Daxis.variable=tomcat | tee build-test-batch.log | file -



#
# This portion of the script uploads the bundle artifacts to S3 to be downloaded by downstream scripts
#

	aws s3 cp --no-progress \
		/opt/dev/projects/github/liferay-portal/liferay-portal-bundle-tomcat.tar.gz \
		s3://liferayci-file-propagator/caylent/builds/[number]/liferay-portal-bundle-tomcat.tar.gz

	aws s3 cp --no-progress \
		/opt/dev/projects/github/liferay-portal/liferay-portal-source.tar.gz \
		s3://liferayci-file-propagator/caylent/builds/[number]/liferay-portal-source.tar.gz



ubuntu@ip-172-31-5-84 /opt/dev/projects/github/bundles
🐈 $ ll -h
total 45M
drwxrwxr-x 8 ubuntu ubuntu 4.0K Jun 14 01:56 .
drwxr-xr-x 5 ubuntu ubuntu 4.0K Jun 14 01:47 ..
-rw-rw-r-- 1 ubuntu ubuntu  31M Jun 14 01:49 glowroot-0.14.2-dist.zip
-rw-rw-r-- 1 ubuntu ubuntu  14M Jun 14 01:48 apache-tomcat-10.1.40.zip
drwxrwxr-x 2 ubuntu ubuntu 4.0K Jun 14 01:49 deploy
drwxrwxr-x 3 ubuntu ubuntu 4.0K Jun 14 01:56 elasticsearch-sidecar
drwxrwxr-x 4 ubuntu ubuntu 4.0K Jun 14 01:54 glowroot
-rw-rw-r-- 1 ubuntu ubuntu    0 Jun 14 01:49 .liferay-home
drwxrwxr-x 7 ubuntu ubuntu 4.0K Jun 14 01:57 osgi
drwxrwxr-x 9 ubuntu ubuntu 4.0K Jun 14 01:49 tomcat-10.1.40
drwxrwxr-x 5 ubuntu ubuntu 4.0K Jun 14 01:52 tools




# Notes:

 [exec]
    [mkdir] Created dir: /opt/dev/projects/github/liferay-portal/modules/_node-scripts/bundle/sass/binary
[mirrors-get] Downloading http://mirrors.lax.liferay.com/github.com/sass/dart-sass/releases/download/1.77.5/dart-sass-1.77.5-linux-x64.tar.gz to /home/ubuntu/.liferay/mirrors/github.com/sass/dart-sass/releases/download/1.77.5/1749865338983dart-sass-1.77.5-linux-x64.tar.gz.
[mirrors-get] Unable to connect to http://mirrors.lax.liferay.com/github.com/sass/dart-sass/releases/download/1.77.5/dart-sass-1.77.5-linux-x64.tar.gz, will retry in 30 seconds.
[mirrors-get] Downloading http://mirrors.lax.liferay.com/github.com/sass/dart-sass/releases/download/1.77.5/dart-sass-1.77.5-linux-x64.tar.gz to /home/ubuntu/.liferay/mirrors/github.com/sass/dart-sass/releases/download/1.77.5/1749865338983dart-sass-1.77.5-linux-x64.tar.gz.
[mirrors-get] Downloading https://github.com/sass/dart-sass/releases/download/1.77.5/dart-sass-1.77.5-linux-x64.tar.gz to /home/ubuntu/.liferay/mirrors/github.com/sass/dart-sass/releases/download/1.77.5/1749865338983dart-sass-1.77.5-linux-x64.tar.gz.
[mirrors-get] Moving /home/ubuntu/.liferay/mirrors/github.com/sass/dart-sass/releases/download/1.77.5/1749865338983dart-sass-1.77.5-linux-x64.tar.gz to /home/ubuntu/.liferay/mirrors/github.com/sass/dart-sass/releases/download/1.77.5/dart-sass-1.77.5-linux-x64.tar.gz.
[mirrors-get] Copying /home/ubuntu/.liferay/mirrors/github.com/sass/dart-sass/releases/download/1.77.5/dart-sass-1.77.5-linux-x64.tar.gz to /opt/dev/projects/github/liferay-portal/modules/_node-scripts/bundle/sass/binary/dart-sass-1.77.5-linux-x64.tar.gz.
    [untar] Expanding: /opt/dev/projects/github/liferay-portal/modules/_node-scripts/bundle/sass/binary/dart-sass-1.77.5-linux-x64.tar.gz into /opt/dev/projects/github/liferay-portal/modules/_node-scripts/bundle/sass/binary
     [echo] setup.yarn.start.timestamp: 06-13-2025 18:42:50:274 PDT
[beanshell] Executing Gradle task: setUpYarnOfflineCache
     [exec] Configuration on demand is an incubating feature.


	  [delete] Deleting: /opt/dev/projects/github/liferay-portal/null297264874.properties

build-dist-tomcat:
   [delete] Deleting: /opt/dev/projects/github/liferay-portal/null297264874.properties

unzip-tomcat:
    [mkdir] Created dir: /opt/dev/projects/github/bundles
[mirrors-get] Downloading          http://mirrors.lax.liferay.com/archive.apache.org/dist/tomcat/tomcat-10/v10.1.40/bin/apache-tomcat-10.1.40.zip to /home/ubuntu/.liferay/mirrors/archive.apache.org/dist/tomcat/tomcat-10/v10.1.40/bin/1749865636085apache-tomcat-10.1.40.zip.
[mirrors-get] Unable to connect to http://mirrors.lax.liferay.com/archive.apache.org/dist/tomcat/tomcat-10/v10.1.40/bin/apache-tomcat-10.1.40.zip, will retry in 30 seconds.


[mirrors-get] Downloading          http://mirrors.lax.liferay.com/archive.apache.org/dist/tomcat/tomcat-10/v10.1.40/bin/apache-tomcat-10.1.40.zip to /home/ubuntu/.liferay/mirrors/archive.apache.org/dist/tomcat/tomcat-10/v10.1.40/bin/1749865636085apache-tomcat-10.1.40.zip.
[mirrors-get] Unable to connect to http://mirrors.lax.liferay.com/archive.apache.org/dist/tomcat/tomcat-10/v10.1.40/bin/apache-tomcat-10.1.40.zip, defaulting to http://archive.apache.org/dist/tomcat/tomcat-10/v10.1.40/bin/apache-tomcat-10.1.40.zip.
[mirrors-get] Downloading http://archive.apache.org/dist/tomcat/t



build.properties:    mirrors.hostname=mirrors.lax.liferay.com
build.properties:    #nodejs.npm.ci.registry=http://mirrors.lax.liferay.com:4873

build-test.xml:                                         <contains string="${patching.tool.version.url}" substring="mirrors.lax.liferay.com" />
build-test.xml:                                                         regexp="mirrors.lax.liferay.com"

test.properties:    test.smtp.server.url=http://mirrors.lax.liferay.com/repository.liferay.com/nexus/content/repositories/third-party/com/liferay/com.mockmock/1.4.0/com.mockmock-1.4.0.jar




    [exec]
     [exec] > Task :apps:frontend-theme:frontend-theme-dialect:assemble
     [exec]
     [exec] > Task :apps:frontend-theme:frontend-theme-dialect:deploy
     [exec] Files of project ':apps:frontend-theme:frontend-theme-dialect' deployed to /opt/dev/projects/github/bundles/osgi/portal-war
     [exec]
     [exec] BUILD SUCCESSFUL in 1m 47s
     [exec] 6285 actionable tasks: 73 executed, 6212 up-to-date
     [exec]
[stopwatch] [modules.stopwatch: 1:49.702 sec]
     [echo] June 14, 2025 at 02:51 AM
   [delete] Deleting: /opt/dev/projects/github/liferay-portal/app.server.ubuntu.properties
[stopwatch] [prepare-test-build-dist.bundles.tomcat.build-dist: 2:10.392 sec]
   [delete] Deleting: /opt/dev/projects/github/liferay-portal/null180042750.properties

prepare-system-ext-properties:

BUILD FAILED
/opt/dev/projects/github/liferay-portal/build-test-batch.xml:3715: The following error occurred while executing this line:
/opt/dev/projects/github/liferay-portal/build-test-batch.xml:977: The following error occurred while executing this line:
/opt/dev/projects/github/liferay-portal/build-test-batch.xml:3148: The following error occurred while executing this line:
/opt/dev/projects/github/liferay-portal/build-test-batch.xml:3301: Property 'java.jdk.default.runtime.version' is not defined.

Total time: 2 minutes 38 seconds
ubuntu@ip-172-31-5-8






## Measuring Time using tar.gz

🐈 $ echo "Start: $(date)" ; time tar -czvf  github-repo-liferay-portal.tar.gz liferay-portal ;  echo "End: $(date)"
Start: Wed Jun 25 07:27:46 AM UTC 2025

liferay-portal/lib/versions.xml
liferay-portal/lib/ide-ignore.txt
liferay-portal/build-test-export-import.xml
liferay-portal/build-test-saml.xml
liferay-portal/git-commit-blade-samples
liferay-portal/gradlew
liferay-portal/find-security-bugs-false-positives.txt

real	10m9.383s
user	4m23.903s
sys	0m45.733s
End: Wed Jun 25 07:37:55 AM UTC 2025


-rw-rw-r--  1 ubuntu ubuntu 4.2G Jun 25 07:37 github-repo-liferay-portal.tar.gz

-rw-rw-r--  1 ubuntu ubuntu 124M Jun 25 07:41 github-repo-liferay-jenkins-ee.tar.gz
-rw-rw-r--  1 ubuntu ubuntu 4.2G Jun 25 07:37 github-repo-liferay-portal.tar.gz

buntu@ip-172-31-5-84 /opt/dev/projects/github
🐈 $ ll
total 4320576
drwxr-xr-x  5 ubuntu ubuntu       4096 Jun 25 07:40 .
drwxr-xr-x  3 root   root         4096 Jun 13 20:45 ..
drwxrwxr-x  8 ubuntu ubuntu       4096 Jun 18 08:38 bundles
-rw-rw-r--  1 ubuntu ubuntu         45 Jun 25 07:40 github-repo-liferay-portal-ee.tar.gz
-rw-rw-r--  1 ubuntu ubuntu 4424239919 Jun 25 07:37 github-repo-liferay-portal.tar.gz
drwxrwxr-x 14 ubuntu ubuntu       4096 Jun 13 20:48 liferay-jenkins-ee
drwxrwxr-x 39 ubuntu ubuntu       4096 Jun 18 08:46 liferay-portal
ubuntu@ip-172-31-5-84 /opt/dev/projects/github
🐈 $ echo "Start: $(date)" ; time tar -czvf  github-repo-liferay-jenkins-ee.tar.gz liferay-jenkins-ee ;  echo "End: $(date)"
Start: Wed Jun 25 07:41:09 AM UTC 2025
liferay-jenkins-ee/
liferay-jenkins-ee/jenkins.properties
liferay-jenkins-ee/.git/
...
liferay-jenkins-ee/verify/verify_slave_ubuntu18_mysql.sh
liferay-jenkins-ee/gradlew

real	0m18.240s
user	0m5.729s
sys	0m1.808s
End: Wed Jun 25 07:41:27 AM UTC 2025



ubuntu@ip-172-31-5-84 /opt/dev/projects/github
🐈 $ ll -h
total 4.3G
drwxr-xr-x  5 ubuntu ubuntu 4.0K Jun 25 07:43 .
drwxr-xr-x  3 root   root   4.0K Jun 13 20:45 ..
drwxrwxr-x  8 ubuntu ubuntu 4.0K Jun 18 08:38 bundles
-rw-rw-r--  1 ubuntu ubuntu 124M Jun 25 07:41 github-repo-liferay-jenkins-ee.tar.gz
-rw-rw-r--  1 ubuntu ubuntu 4.2G Jun 25 07:37 github-repo-liferay-portal.tar.gz
drwxrwxr-x 14 ubuntu ubuntu 4.0K Jun 13 20:48 liferay-jenkins-ee
drwxrwxr-x 39 ubuntu ubuntu 4.0K Jun 18 08:46 liferay-portal


echo "Start: $(date)" ; time tar -czf  github-repo-liferay-jenkins-ee.tar.gz liferay-jenkins-ee ;  echo "End: $(date)"
echo "Start: $(date)" ; time tar -czf  github-repo-liferay-portal.tar.gz     liferay-portal ;      echo "End: $(date)"


echo "Start: $(date)" ; time tar -cf  github-repo-liferay-jenkins-ee-pigz.tar.gz --use-compress-program=pigz  liferay-jenkins-ee ;  echo "End: $(date)"
echo "Start: $(date)" ; time tar -cf  github-repo-liferay-portal-pigz.tar.gz     --use-compress-program=pigz  liferay-portal ;      echo "End: $(date)"

                            tar -cf archive.tar.gz --use-compress-program=pigz folder/

# Files compressed with pigz are fully compatible with gzip, 
# because pigz just speeds up compression using multiple cores but still produces standard .gz files.


🐈 $ echo "Start: $(date)" ; time tar -cf  github-repo-liferay-portal-pigz.tar.gz     --use-compress-program=pigz  liferay-portal ;      echo "End: $(date)"
Start: Wed Jun 25 07:55:56 AM UTC 2025

real	0m41.470s
user	4m3.842s
sys	0m14.293s
End: Wed Jun 25 07:56:38 AM UTC 2025



ubuntu@ip-172-31-5-84 /opt/dev/projects/github
🐈 $ ll
total 8894792
drwxr-xr-x  5 ubuntu ubuntu       4096 Jun 25 07:55 .
drwxr-xr-x  3 root   root         4096 Jun 13 20:45 ..
drwxrwxr-x  8 ubuntu ubuntu       4096 Jun 18 08:38 bundles
-rw-rw-r--  1 ubuntu ubuntu  129320307 Jun 25 07:54 github-repo-liferay-jenkins-ee-pigz.tar.gz
-rw-rw-r--  1 ubuntu ubuntu  129234500 Jun 25 07:41 github-repo-liferay-jenkins-ee.tar.gz
-rw-rw-r--  1 ubuntu ubuntu 4425436354 Jun 25 07:56 github-repo-liferay-portal-pigz.tar.gz
-rw-rw-r--  1 ubuntu ubuntu 4424239919 Jun 25 07:37 github-repo-liferay-portal.tar.gz
drwxrwxr-x 14 ubuntu ubuntu       4096 Jun 13 20:48 liferay-jenkins-ee
drwxrwxr-x 39 ubuntu ubuntu       4096 Jun 18 08:46 liferay-portal

ubuntu@ip-172-31-5-84 /opt/dev/projects/github
🐈 $ echo "Start: $(date)" ; time tar -czf  github-repo-liferay-portal.tar.gz     liferay-portal ;      echo "End: $(date)"
Start: Wed Jun 25 07:59:37 AM UTC 2025

real	4m41.553s
user	4m22.361s
sys	0m16.200s
End: Wed Jun 25 08:04:18 AM UTC 2025

ubuntu@ip-172-31-5-84 /opt/dev/projects/github
🐈 $ ll
total 8894792
drwxr-xr-x  5 ubuntu ubuntu       4096 Jun 25 07:59 .
drwxr-xr-x  3 root   root         4096 Jun 13 20:45 ..
drwxrwxr-x  8 ubuntu ubuntu       4096 Jun 18 08:38 bundles
-rw-rw-r--  1 ubuntu ubuntu  129320307 Jun 25 07:54 github-repo-liferay-jenkins-ee-pigz.tar.gz
-rw-rw-r--  1 ubuntu ubuntu  129234500 Jun 25 07:41 github-repo-liferay-jenkins-ee.tar.gz
-rw-rw-r--  1 ubuntu ubuntu 4425436354 Jun 25 07:56 github-repo-liferay-portal-pigz.tar.gz
-rw-rw-r--  1 ubuntu ubuntu 4424238971 Jun 25 08:04 github-repo-liferay-portal.tar.gz
drwxrwxr-x 14 ubuntu ubuntu       4096 Jun 13 20:48 liferay-jenkins-ee
drwxrwxr-x 39 ubuntu ubuntu       4096 Jun 18 08:46 liferay-portal

pigz -dc github-repo-liferay-portal-pigz.tar.gz | tar -xvf -

cp github-repo-liferay-jenkins-ee.tar.gz s3://liferayci-file-propagator/repo/
cp github-repo-liferay-portal.tar.gz     s3://liferayci-file-propagator/repo/
