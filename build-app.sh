#!/bin/bash
set -e

echo "🚀 [1/2] Building JAR with Maven..."
./mvnw clean package

echo "🍏 [2/2] Packaging Standalone MacNetMeter.app..."
rm -rf dist
/Library/Java/JavaVirtualMachines/graalvm-jdk-21/Contents/Home/bin/jpackage \
  --type app-image \
  --input target \
  --name MacNetMeter \
  --main-jar MacNetMeter-1.0.0-jar-with-dependencies.jar \
  --main-class com.maknoon.App \
  --dest dist \
  --java-options "-Dapple.awt.UIElement=true"

echo "🎉 DONE! Your app is ready at dist/MacNetMeter.app"