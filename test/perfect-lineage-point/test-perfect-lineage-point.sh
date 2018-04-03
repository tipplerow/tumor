#!/bin/sh

cd `dirname $0`

${JAM_HOME}/bin/jam-test.sh \
           "${TUMOR_HOME}/bin/perfect-lineage-point.sh perfect-lineage-point.prop" \
           gold \
           cell-count-stat.csv \
           cell-count-traj.csv
