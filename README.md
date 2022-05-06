# DEVILS: Display Enhancement For Visual Inspection of Large Stacks

**Once you summon DEVILS, you cannot quantify your image anymore! This tool is made for visual inspection ONLY!**

This repository is the Fiji implementation of the following publication:
- *Guiet R, Burri O, Chiaruttini N, Hagens O and Seitz A* **DEVILS: a tool for the visualization of large datasets with a high dynamic range** 
[version 1; peer review: 1 approved with reservations]. F1000Research 2020, 9:1380 (https://doi.org/10.12688/f1000research.25447.1)

Please cite this paper if you make use of DEVILS in your research.

**Supplemental Data and Examples**

All the data used in the paper can be found on FigShare:

[Data: DEVILS: a tool for the visualization of large datasets with a high dynamic range](https://doi.org/10.6084/m9.figshare.c.5197940.v2)

## What is DEVILS?

DEVILS is an image processing plugin for Fiji that allows a human to observe features within several magnitudes of intensities.

The plugin works by homogenizing intensities and removing global and local background intensities in order to display high and low intensities without having to constantly change the display range of the data.

### Example Image
![Example DEVILS Application](https://raw.githubusercontent.com/BIOP/ijp-DEVILS/master/images/top_original_bottom_processed.jpg)
**Top: Original 3 Channel Image, Bottom : DEVILS result.**
*Sample preparation* : Olivier Hagens, LNMC, EPFL 
*Image acquisition* : Thierry Laroche, BIOP, EPFL 
*Image enhancement* : Romain Guiet, BIOP, EPFL

## What does DEVILS mean?

DEVILS stands for **Display Enhancement for Visual Inspection of Large Stacks**. This tool is made for **visual inspection ONLY! NO measurements! NO quantifications!**

## Under the Hood

DEVILS is a Fiji plugin which uses a combination of three plugins/functions:
-   Division by a Gaussian blurred version of the image
-   Per pixel square root calculation
-   [Rolling ball background subtraction](https://imagej.net/Rolling_Ball_Background_Subtraction)

These operations themselves are rather straightforward. However, DEVILS brings with it the advantage of parallel processing the input image plane by plane in order to gain time when working with large datasets, such as those coming from SPIM.

## What are the drawbacks of DEVILS?

DEVILS increases the noise levels on your image by a significant amount! This makes it suitable for high signal-to-noise images, otherwise the noise contribution will be greatly enhanced. 
Because of the non-global operations of DEVILS, intensities are modified in such a way that you cannot draw any conclusion based on their values. 

# Installation
DEVILS is a part of the PTBIOP [Update Site](https://imagej.net/Update_Sites). 
Using Fiji, you can install it directy by going to:
`Help > Update... > Manage Update Sites`
Scroll down until you find `PTBIOP` and check the box on the left.
Close the updater windows and restart Fiji.

All DEVILS commands are accessible from `Plugins > BIOP > Image Processing`

## Manual Installation

Because DEVILS depends on a particular ecosystem of plugins, we do not offer a fat JAR or a means to download it other than the update site. This ensures all dependencies are present and managed by Fiji itself.

# GUI & Parameters

DEVILS aims to be simple to use in terms of parameters. There are two main Fiji commands for DEVILS: a **Preview Command**, which works on the currently open image, and the main **DEVILS Command** that works from a file on disk. 
![Accessing the DEVILS Commands](https://raw.githubusercontent.com/BIOP/ijp-DEVILS/master/images/DEVILS-menu-access.png) These commands are further split into a basic mode and an advanced mode.

# Basic Mode

## DEVILS Preview ( Basic )
This preview command works from an opened image. 
![DEVILS Preview ( Basic )](https://raw.githubusercontent.com/BIOP/ijp-DEVILS/master/images/DEVILS-preview-basic-full_2.png)
When launched, it creates a (virtual) copy of the active image stack, and displays the effect of DEVILS as you change the "Largest object size" parameter and press the "Create or Update Preview" button. 

**NOTE** The responsiveness of the Preview mode is highly dependent on the size of your dataset. We recommend that you **crop** your dataset to a manageable size (512x512 pixels in XY) before running it. Otherwise the interface might be slow and unresponsive.

### Largest object size (in pixel) parameter
This parameter fixes the internal values of the gaussian blur sigma and the object size for the local background subtraction. It should be set to the size of the largest object in your image (Usually cells, cell nuclei or filaments). In the case of filament-like structures, it would correspond to the largest filament thickness you are interested in.

### Start DEVILS with the current parameter
This button starts the basic DEVILS interface (see below), with the current value of "Largest object size (in pixel)".  

## DEVILS Interface

![DEVILS Main GUI](https://raw.githubusercontent.com/BIOP/ijp-DEVILS/master/images/DEVILS-main-gui.png)

### Select File

As DEVILS is intended for large images, this command does not work with an image currently opened in Fiji, but rather will use BioFormats to open a potential large image and process it plane by plane, ideally never having to load the entire dataset into the RAM of the PC.

### Largest object size (in pixel)

This parameter fixes the internal values of the gaussian blur sigma and the object size for the local background subtraction. It should be set to the size of the largest object in your image (Usually cells, cell nuclei or filaments). In the case of filament-like structures, it would correspond to the largest filament thickness you are interested in.

## Optional Parameters
### Export to XML Hdf5

Use this option in order to generate a pyramidal HDF5 File compatible with [BigDataViewer](https://imagej.net/BigDataViewer). This is useful for subsequent visualization and processing in case your data is very large. 

### Advanced Parameters

Selecting this checkbox will bring up the DEVILS advanced parameters window after you click OK (see below).

# Advanced Mode

## DEVILS Preview ( Advanced )
The preview command works on the currently open image.
The advanced preview allows you to specify different parameters for each image channel, as well as other internal parameters that are otherwise set to sensible defaults in the Basic mode (min at -100, max at 10000).
![DEVILS Preview ( Advanced) GUI](https://raw.githubusercontent.com/BIOP/ijp-DEVILS/master/images/DEVILS-preview-advanced-gui.png)

When launched, the user interface waits for you to define the parameters for each channel (using comma separated value). 
Once you have defined the parameters, press the "Create and Update Preview" button to create a (virtual) copy of the active image stack.
You can now navigate between channels and slices to visualize the effect of the current parameters. The processing of the current plane is done on the fly and might take a few seconds depending of your image size.
Shall you change parameters , please click on "Create and Update Preview" button.

**NOTE** : 
- You might need to adjust the "Brightness & Contrast" of the DEVILS image
- We recommend to stay with the grayscale display (rather than composite mode) 
- The responsiveness of the Preview mode is highly dependent on the size of your dataset. We recommend that you **crop** your dataset to a manageable size (512x512 pixels in XY) before running it. Otherwise the interface might be slow and unresponsive.


### Largest object size (in pixel) parameter

Same as in basic mode, except you can now define a largest object size for each channel in your image by separating them with commas. You must specify the same number of values as there are channels. 

### Minimum (resp Maximum) for final conversion step

Because DEVILS modifies the intensity values though the Gaussian division, square root and background subtraction, the final DEVILS image will have an arbitrary intensity range. In order to rescale this range linearly to an 8-bit or 16-bit  output, values must be chosen for the minimum intensity (which will correspond to 0 intensity afterwards, anything below will be clipped to 0) and the maximum (which will correspond to 255 intensity or 65535 intensity for 8-bit and 16-bit respectively, anything above will be clipped).
You need to select the values for each channel independently and the number of parameters must be identical to the number of channels in your image. 
In basic mode, the minimum and maximum values are hard-set to -100 and 10'000 for each channel. 
See [How to choose the advanced parameters](#How-to-choose-the-advanced-parameters) below.

### Start DEVILS with the current parameter
This button starts the advanced DEVILS interface (see below) with the current parameter values. If you start the advanced DEVILS interface from the preview, the pre-filled parameters will not show up in the advanced interface.

## How to choose the advanced parameters

Our suggestion is to use :

Brute force : by run DEVILS without advanced parameters on a subset of your image. DEVILS will print the minimum and maximum values of this dataset to the ImageJ Log window, which you can then use when processing your full dataset.
 
DEVILS Preview : 
 - Crop a part of your image with a high dynamic range,
 - Run DEVILS Preview (Advanced), 
 - Select output 32-bit
 - "Create and Update Preview" button, 
 - Open histogram of the image (```Analyze > Histogram```)
 - Look at minimum and maximum value.

This is especially useful when batch processing multiple files, as this ensures that all files have been treated the same way by DEVILS when using the Advanced mode. 

## DEVILS Advanced Parameters

This window pops up automatically when you check the **Advanced Parameters** checkbox from the DEVILS Command.

![DEVILS Main GUI with Advanced Parameters Checked](https://raw.githubusercontent.com/BIOP/ijp-DEVILS/master/images/DEVILS-main-gui-advanced-checked.png)

![DEVILS Advanced parameters GUI](https://raw.githubusercontent.com/BIOP/ijp-DEVILS/master/images/DEVILS-advanced-gui.png)

These parameters allow you to fine-tune the output of DEVILS in different ways.

### Output Directory

If left empty, DEVILS will automatically create an 'output' folder in the same folder as the original image. 

It can be useful to set it to a different disk in order to maximize the read-write speed of DEVILS. In case the DEVILS image is written on the same disk as the one from which the image is read, a drop in performance can be perceived, as reading and writing is happening on the same disk.

### Minimum (resp Maximum) for final conversion step

See [above](#minimum-resp-maximum-for-final-conversion-step) for a description of these options.

### Output Bit Depth

This is used in conjunction with the Minimum and Maximum values, and helps to reduce the size of the DEVILS image. As the idea of DEVILS is to view wildly varing intensities efficiently by humans, a **8-bit** output is often more than enough when the **Minimum** and **Maximum** values are appropriately set for your data. 
You can use the **32-bit** output to see the **raw non-rescaled DEVILS image** in order to estimate the **Minimum** and **Maximum** values to use for your dataset. We suggest you do this on a small subset of your data.

# What is the output of DEVILS (basic and advanced) plugin ?

DEVILS generates a series of tiff files, each file being a single z-plane of a single channel of a single timepoint. 
If your original image consists of a single z-stack or a simple time series, the processed image can be simply opened via `File>Import>Image Sequence...`.
However, more complex 4D or 5D stacks, as well as multi series images, requires additional information from the original image in order to be re-opened properly.

That's why, in addition to tiff files, an additional `DEVILSParameters.json` file is created, which contains some metadata informations, as well as the parameters used to process the image.
This file can be used in order to open conveniently the processed in ImageJ (see `Opening TIFF Files / Opening DEVILS Folder`).

In addition to these tiffs + json file, if you selected the associated option, you also have a nested folder which can contain an xml file and a hdf5 file (see `Opening HDF5/XML Files`).

### Opening TIFF Files / Opening DEVILS Folder

The tiff files can be opened using `File > Import > Image Sequence...` and defining the correct number of channels, slices and frames.  
Instead of `Image Sequence`, the **Open DEVILS Folder** Command opens a DEVILS folder as a Virtual Stack by pointing it to the `DEVILSParameters.json` file in the output folder of DEVILS. This avoids the inconvenience of having to define the structure of the sequence by hand when opening a DEVILS result.

### Opening HDF5/XML Files

The HDF5 file can be opened with `Plugins > BigDataViewer > Open XML/HDF5`

# Tutorial, Example

We provide a step-by-step tutorial for DEVILS in ZENODO. 

Please download this entry containing an **example dataset**, and a **PDF workflow** that contains the tutorial by clicking on the DOI badge below:

ZENODO DOI: [![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.4058414.svg)](https://doi.org/10.5281/zenodo.4058414)
