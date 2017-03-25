#!/usr/bin/env bash

./gradlew clean build -x test
java $JAVA_OPTS -Dspring.profiles.active=stage -jar build/libs/*aqar-*.jar
