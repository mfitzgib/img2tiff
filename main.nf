#!/usr/bin/env nextflow

nextflow.enable.dsl = 2

process convert_vsi {
    publishDir params.output, overwrite: true, mode: "copy"
    container "${params.container}"

    input:
        path "inputs"
        path script

    output:
        path "*.ome.tif"
        path "*.log.txt"
        path "*.metadata.json"

    script:
        """#!/bin/bash
set -e

for VSI_FILE in inputs/*.vsi; do
    echo "\$(date) Converting \$VSI_FILE"
    LOG_TXT="\${VSI_FILE#inputs/}.log.txt"
    QuPath script --args \$VSI_FILE --args "\$PWD" "${script}" | tee "\$LOG_TXT"
done
echo "\$(date) Done"
        """
}

workflow {
    script = file("$projectDir/vsi2tiff_headless.groovy", checkIfExists: true)
    input_folder = file(params.input_folder, type: 'dir', checkIfExists: true)
    convert_vsi(input_folder, script)
}