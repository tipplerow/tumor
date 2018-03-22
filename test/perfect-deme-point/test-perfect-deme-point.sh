#!/bin/sh

cd `dirname $0`

${JAM_HOME}/bin/jam-test.sh \
           "${TUMOR_HOME}/bin/perfect-deme-point.sh perfect-deme-point.prop" \
           gold \
           cell-count-stat.csv \
           cell-count-traj.csv
