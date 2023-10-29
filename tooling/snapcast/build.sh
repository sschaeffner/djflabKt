#!/bin/bash

cd "$(dirname "$0")"

podman-compose --podman-build-args "--ulimit nofile=65535:65535" build