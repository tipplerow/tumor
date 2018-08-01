#!/bin/sh
########################################################################
# Driver script for neoantigen simulation.
########################################################################

PROP_FILE=neoantigen-prod.prop

if [ $# -ne 3 ]
then
    echo "Usage:" `basename $0` "SITE_CAPACITY SELECTION_COEFF NEOANTIGEN_RATE"
    exit 1
fi

SITE_CAPACITY=$1
SELECTION_COEFF=$2
NEOANTIGEN_RATE=$3

TRIAL_BEGIN=0
TRIAL_END=50

REPORT_BASE=`dirname $0`
cd $REPORT_BASE

TrialIndex=$TRIAL_BEGIN

while [ $TrialIndex -lt $TRIAL_END ]
do
    SubDir=$(printf "C%s/S%.2f/R%s/Trial%02d" $SITE_CAPACITY $SELECTION_COEFF $NEOANTIGEN_RATE $TrialIndex)

    if [ ! -d $SubDir ]
    then
        mkdir -p $SubDir
    fi

    tumor-driver.sh -Xmx96G \
                    -Djam.app.reportDir=${REPORT_BASE}/$SubDir \
                    -Dtumor.driver.trialIndex=$TrialIndex \
                    -Dtumor.capacity.siteCapacity=$SITE_CAPACITY \
                    -Dtumor.mutation.selectionCoeff=$SELECTION_COEFF \
                    -Dtumor.mutation.neoantigenMeanRate=$NEOANTIGEN_RATE \
                    $PROP_FILE

    TrialIndex=$(expr $TrialIndex + 1)
done

exit 0
