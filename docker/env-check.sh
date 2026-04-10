#!/bin/sh

if [ -z "${CONTAINER_HEALTH_FLAG}" ]; then
  echo "CONTAINER_HEALTH_FLAG is not set" >&2
fi