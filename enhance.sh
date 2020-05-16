#!/usr/bin/env bash
set -euo pipefail

if [ -d "classes/cte" ]; then rm -r classes/cte; fi
if [ -d "enhanced/cte" ]; then rm -r enhanced/cte; fi
javac -cp datanucleus-accessplatform-rdbms-5.1.9/deps/javax.jdo-3.2.0-m8.jar -d classes *.java
export JAVA_OPTS='-cp classes:datanucleus-accessplatform-rdbms-5.1.9/lib/*:datanucleus-accessplatform-rdbms-5.1.9/deps/*'
java $JAVA_OPTS org.datanucleus.enhancer.DataNucleusEnhancer -dir classes -d enhanced
