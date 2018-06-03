#!/bin/sh

cd `dirname $0`

${JAM_HOME}/bin/jam-test.sh \
           "${TUMOR_HOME}/bin/tumor-driver.sh -ea point-deme-neutral.prop" \
           gold \
           accumulated-mutations.csv.gz \
           cell-count-traj.csv \
           component-ancestry.csv.gz \
           final-cell-count.csv \
           original-mutations.csv.gz
