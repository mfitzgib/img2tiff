## img2tiff

Convert image to OME-TIFF format (pyramid, JPEG pending).
Tested on QuPath 0.6.0 under macOS, both arm64 and x86 emulated,
in headless mode.


## Nextflow Workflow

Parameters:
  - input_folder: URI to folder which contains the images to convert
  - image_format: File suffix for images which will be converted
  - output: Folder where results will be published
  - container: Docker container used to run QuPath (default: 'public.ecr.aws/cirrobio/qupath:0046339')

Behavior:
Any file within `input_folder` which has the `image_format` suffix will be provided
as an input to the `img2tiff_headless.groovy` script within QuPath.
Every independent series within each image will be written out as a separate
OME-TIFF file.


## Standalone Script: img2tiff_headless.groovy

Arguments:
  - param1: input VSI image path
  - param2: series number (optional, defaults to 1)

The H&amp;E images we work with have at least four series:

0. Slide Label
1. Medium resolution overview
2. Full resolution (e.g. 40X brightfield)

The full resolution image seems to take around 10 minutes to
convert an image on 2 cores of an M1 Apple Silicon MacBook Pro.