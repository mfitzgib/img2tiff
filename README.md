## vsi2tiff
Notes on working with Olympus VS200 .vsi images,

### vsi2tiff_headless.groovy
Convert VSI image to OME-TIFF format (pyramid, JPEG pending).
Tested on QuPath 0.6.0 under macOS, both arm64 and x86 emulated,
in headless mode.

Arguments:
  - param1: input VSI image path
  - param2: series number (optional, defaults to 1)

The H&amp;E images we work with have at least four series:

0. Slide Label
1. Medium resolution overview
2. Full resolution (e.g. 40X brightfield)

The full resolution image seems to take around 10 minutes to
convert an image on 2 cores of an M1 Apple Silicon MacBook Pro.