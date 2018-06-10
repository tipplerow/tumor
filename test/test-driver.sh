#!/bin/sh

JAM_TEST=${JAM_HOME}/bin/jam-test.sh
TUMOR_DRIVER=${TUMOR_HOME}/bin/tumor-driver.sh
DRIVER_PROP=driver.prop
GOLD_DIR=gold

cd $1
shift

echo "Testing in directory [`pwd`]..."

$JAM_TEST "$TUMOR_DRIVER -ea $DRIVER_PROP" $GOLD_DIR "$@"
