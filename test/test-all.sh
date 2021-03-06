#!/bin/sh
########################################################################
# Runs all application regression tests.
########################################################################

cd `dirname $0`

./test-driver.sh \
    Neoantigen/neoantigen-test.prop \
    bulk-site-mutation-type-count.csv \
    surface-site-mutation-type-count.csv \
    runtime.prop

exit 0

./test-driver.sh \
    LatticeCell/lattice-cell-test.prop \
    component-coord.csv.gz \
    component-count.csv \
    mutation-count.csv \
    runtime.prop

./test-driver.sh BulkSampleSite/bulk-sample-site-test.prop bulk-sample-site.csv

BASE_FILES="cell-count-traj.csv component-ancestry.csv.gz final-cell-count.csv"
COORD_FILES="component-coord.csv.gz"
MUTATION_FILES="accumulated-mutations.csv.gz original-mutations.csv.gz scalar-mutations.csv.gz"

./test-driver.sh driver/point/perfect/cell    $BASE_FILES || exit $?
./test-driver.sh driver/point/perfect/deme    $BASE_FILES || exit $?
./test-driver.sh driver/point/perfect/lineage $BASE_FILES || exit $?

./test-driver.sh driver/point/neutral/cell    $BASE_FILES $MUTATION_FILES || exit $?
./test-driver.sh driver/point/neutral/deme    $BASE_FILES $MUTATION_FILES || exit $?
./test-driver.sh driver/point/neutral/lineage $BASE_FILES $MUTATION_FILES || exit $?

./test-driver.sh driver/lattice/neutral/cell    $BASE_FILES $COORD_FILES || exit $?
./test-driver.sh driver/lattice/neutral/deme    $BASE_FILES $COORD_FILES || exit $?
./test-driver.sh driver/lattice/neutral/lineage $BASE_FILES $COORD_FILES || exit $?

./test-driver.sh driver/lattice/perfect/cell    $BASE_FILES $COORD_FILES || exit $?
./test-driver.sh driver/lattice/perfect/deme    $BASE_FILES $COORD_FILES || exit $?
./test-driver.sh driver/lattice/perfect/lineage $BASE_FILES $COORD_FILES || exit $?

exit 0
