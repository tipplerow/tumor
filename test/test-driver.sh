#!/bin/sh
########################################################################
# Driver script for regression testing of complete simulations.
########################################################################

if [ $# -lt 2 ]
then
    echo "Usage:" `basename $0` "DriverProp File1 [File2 ...]"
    exit 1
fi

JAM_TEST=${JAM_HOME}/bin/jam-test.sh
TUMOR_DRIVER=${TUMOR_HOME}/bin/tumor-driver.sh
GOLD_DIR=gold

DriverProp=$1
shift

ReportDir=`dirname $DriverProp`
DriverProp=`basename $DriverProp`

cd $ReportDir
echo "Testing in directory [`pwd`]..."

$JAM_TEST "$TUMOR_DRIVER -ea $DriverProp" $GOLD_DIR "$@"
