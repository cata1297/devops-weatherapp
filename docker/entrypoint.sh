#!/bin/sh
set -e

(
  while true
  do
    /usr/local/bin/env-check.sh
    sleep 3600
  done
) &

exec java -jar /app/app.jar