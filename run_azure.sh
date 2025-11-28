#!/bin/bash
export SPRING_PROFILES_ACTIVE=local
export SERVER_PORT=8081
./gradlew bootRun
