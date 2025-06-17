#!/bin/bash

FILE="src/main/java/com/liferay/App.java"

for i in {1..10}; do

  sed -i "s/String myCommitNro = \".*\";/String myCommitNro = \"$i\";/" "$FILE"
  git add "$FILE"
  git commit -m "Brute-force commit #$i to main branch - in SampleApp_01"

  git push origin main  
  sleep 2
  
done


