#!/usr/bin/env nextflow

nextflow.enable.dsl = 2

process convert_images {
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

for IMG_FILE in inputs/*.${params.image_format}; do
    echo "\$(date) Converting \$IMG_FILE"
    LOG_TXT="\${IMG_FILE#inputs/}.log.txt"
    QuPath script \
        --args \$IMG_FILE \
        --args "\$PWD" \
        --args "${params.compression}" \
        --args "${params.image_format}" \
        "${script}" | tee "\$LOG_TXT"
done
echo "\$(date) Done"
        """
}

workflow {
    script = file("$projectDir/img2tiff_headless.groovy", checkIfExists: true)
    input_folder = file(params.input_folder, type: 'dir', checkIfExists: true)
    convert_images(input_folder, script)
}