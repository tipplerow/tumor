#!/bin/sh

cd `dirname $0`

${JAM_HOME}/bin/jam-test.sh \
           "${TUMOR_HOME}/bin/tumor-driver.sh -ea point-cell-neutral.prop" \
           gold \
           accumulated-mutations.csv.gz \
           cell-count-traj.csv \
           final-cell-count.csv \
           original-mutations.csv.gz