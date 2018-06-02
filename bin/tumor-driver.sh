#!/bin/sh
########################################################################
# Usage: tumor-driver.sh [JVM OPTIONS] FILE1 [FILE2 ...]
########################################################################

if [ -z "${JAM_HOME}" ]
then
    echo "Environment variable JAM_HOME is not set; exiting."
    exit 1
fi

if [ -z "${TUMOR_HOME}" ]
then
    echo "Environment variable TUMOR_HOME is not set; exiting."
    exit 1
fi

SCRIPT=`basename $0`
JAMRUN=${JAM_HOME}/bin/jam-run.sh

# -------------------------------------------------
# Extract any JVM flags beginning with a hyphen "-"
# -------------------------------------------------

JVM_FLAGS=""

while [[ "$1" == -* ]]
do
    JVM_FLAGS="${JVM_FLAGS} $1"
    shift
done

if [ $# -lt 1 ]
then
    echo "Usage: $SCRIPT [JVM OPTIONS] FILE1 [FILE2 ...]"
    exit 1
fi

$JAMRUN ${TUMOR_HOME} $JVM_FLAGS tumor.driver.TumorDriver "$@"
