#!/bin/sh

cd `dirname $0`

${JAM_HOME}/bin/jam-test.sh \
           "${TUMOR_HOME}/bin/tumor-driver.sh -ea point-lineage-perfect.prop" \
           gold \
           cell-count-traj.csv \
           final-cell-count.csv
