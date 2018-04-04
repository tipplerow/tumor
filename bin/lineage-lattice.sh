#!/bin/sh
########################################################################
# Usage: lineage-lattice.sh FILE1 [FILE2 ...]
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

if [ $# -lt 1 ]
then
    echo "Usage: $SCRIPT FILE1 [FILE2 ...]"
    exit 1
fi

$JAMRUN ${TUMOR_HOME} tumor.driver.LineageLatticeDriver "$@"
