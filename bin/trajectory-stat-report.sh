#!/bin/sh
########################################################################
# Usage: trajectory-stat-report.sh INFILE OUTFILE
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

if [ $# -ne 2 ]
then
    echo "Usage: $SCRIPT INFILE OUTFILE"
    exit 1
fi

$JAMRUN ${TUMOR_HOME} tumor.report.TrajectoryStatReport "$@"
