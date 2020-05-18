#!/usr/bin/env bash
set -euo pipefail

groovy -cp "enhanced:datanucleus-accessplatform-rdbms-5.1.9/lib/*:datanucleus-accessplatform-rdbms-5.1.9/deps/*" -Dlog4j.configuration=file:log4j.properties cte.groovy
