# Dockerfile for alpine-java-git
# Build using:
# docker build --tag gcr.io/startup-os/alpine-java-git - < dockerfile
# Push using:
# docker push gcr.io/startup-os/alpine-java-git

FROM openjdk:8u181-jdk-alpine3.8

RUN apk --update add git openssh && \
    rm -rf /var/lib/apt/lists/* && \
    rm /var/cache/apk/*
