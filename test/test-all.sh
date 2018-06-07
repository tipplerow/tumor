#!/bin/sh
########################################################################
# Runs all application regression tests.
########################################################################

cd `dirname $0`

BASE_FILES="cell-count-traj.csv component-ancestry.csv.gz final-cell-count.csv"
COORD_FILES="component-coord.csv.gz"
MUTATION_FILES="accumulated-mutations.csv.gz original-mutations.csv.gz scalar-mutations.csv.gz"

./test-driver.sh driver/point/perfect/cell    $BASE_FILES || exit $?
./test-driver.sh driver/point/perfect/deme    $BASE_FILES || exit $?
./test-driver.sh driver/point/perfect/lineage $BASE_FILES || exit $?

./test-driver.sh driver/point/neutral/cell    $BASE_FILES $MUTATION_FILES || exit $?
./test-driver.sh driver/point/neutral/deme    $BASE_FILES $MUTATION_FILES || exit $?
./test-driver.sh driver/point/neutral/lineage $BASE_FILES $MUTATION_FILES || exit $?

./test-driver.sh driver/lattice/perfect/cell    $BASE_FILES $COORD_FILES || exit $?
./test-driver.sh driver/lattice/perfect/deme    $BASE_FILES $COORD_FILES || exit $?
./test-driver.sh driver/lattice/perfect/lineage $BASE_FILES $COORD_FILES || exit $?

exit 0
