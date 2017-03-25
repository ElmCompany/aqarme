#!/usr/bin/env bash

docker run --name postgres \
    -e POSTGRES_USER=admin \
    -e POSTGRES_PASSWORD=123456 \
    -e POSTGRES_DB=aqarme \
    -p 5432:5432 \
    -d postgres
