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

    script:
        """#!/bin/bash
set -e

for IMG_FILE in inputs/*.${params.image_format}; do
    OUTPUT_PREFIX=\${IMG_FILE##*/}
    OUTPUT_PREFIX=\${OUTPUT_PREFIX%.${params.image_format}}
    echo "\$(date) Converting \$IMG_FILE (Output Prefix: \$OUTPUT_PREFIX)"
    LOG_TXT="\${OUTPUT_PREFIX}.log.txt"
    QuPath convert-ome \
             \$IMG_FILE \
             \$OUTPUT_PREFIX \
             --pyramid-scale=2 \
             --tile-size=1024 \
             --compression=ZLIB \
             --parallelize | tee "\$LOG_TXT"

    # QuPath script --args \$IMG_FILE --args "\$PWD" "${script}" | tee "\$LOG_TXT"
done
echo "\$(date) Done"
        """
}

workflow {
    script = file("$projectDir/img2tiff_headless.groovy", checkIfExists: true)
    input_folder = file(params.input_folder, type: 'dir', checkIfExists: true)
    convert_images(input_folder, script)
}