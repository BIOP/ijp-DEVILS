# DEVILS 

**Once you summon the DEVILS, you can't quantify your image anymore! This tool is made for visual inspection ONLY!**

## What is DEVILS useful for? 

DEVILS plugin is useful to create an image that shows features within several magnitudes of intensities.

DEVILS plugin homogenizes the intensities and removes (global and local) background so you can display high and low intensities using one display scale.

**Top : Original**

<img src="https://github.com/BIOP/ijp-DEVILS/raw/master/images/top_original_bottom_processed.jpg" title="DEVILS" width="75%" align="center">

**Bottom : DEVILS**

**Sample preparation** : Olivier Hagens, LNMC, EPFL
**Image acquisition** : Thierry Larroche, BIOP, EPFL
**Image enhancement** : Romain Guiet, BIOP, EPFL


## What does DEVILS mean?

DEVILS stands for **Display Enhancement for Visual Inspection in Large Stack**.
This tool is made for **visual inspection ONLY! NO measurement! NO Quantification!** No p values nor some * at the end!

## What is DEVILS exactly?
It's a simple workflow that combines several classic Image Processing tools to decrease the difference between high and low-intensity signals within an image.

## What is DEVILS based on?
DEVILS is an  ImageJ plugin (see below Install).

DEVILS uses a combination of three plugins/functions of ImageJ/Fiji:

- Division by a blurred version of the image
- Square Root Calculation
- Subtract Background

## What are the DEVILS drawbacks?
It increases the noise, by a significant amount !
So your images should use as much display range as possible (without saturation of course) to get a satisfying result.

# Install

Find more [installation instruction here](https://c4science.ch/w/bioimaging_and_optics_platform_biop/image-processing/imagej_tools/devils/ijp-devils/)
