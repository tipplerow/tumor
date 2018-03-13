#!/bin/sh

${JAM_HOME}/bin/jam-test.sh \
           "${TUMOR_HOME}/bin/point-perfect.sh point-perfect.prop" gold \
           size-ratio-stat.csv \
           size-ratio-traj.csv \
           tumor-size-traj.csv

