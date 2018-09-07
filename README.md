# Efficient Image Labeling -- Android Frontend

This repository is Android application (Frontend) of 2017 Fall University of Michigan - Shanghai Jiao Tong University Joint Institute Capstone Project (VE450) 

## Introduction of Project & Contributers

#### Project Background

The field of auto driving is developing rapidly nowadays. In order to realize the function of auto driving, the system needs to identify all the objects on the road so that it can figure it out where to drive the car. While the technology of deep learning can be used as a possible solution for dealing with tasks like this, it requires a large amount of data for the model training.

In this case of auto driving, the image labeling data that frames the objects and detects what they are is needed. The larger the amount of the data, the better the trained model works. Thus, how to acquire such large amount of image labeling data effectively and efficiently becomes a problem.

#### Elementary Design & Concept Description

1. Pre-Process the image on the server (mysql+php+Python)
2. Send the Figure & the pre-processed labelling information to Android Client
3. Modify the frames & labels manually on Android Client
4. Send the label data back to the Server to save the information

## User Guide of Android Frontend

1. Request Image from Server

<img src="
https://charleszhu.weebly.com/uploads/8/3/6/2/83625186/capstone4_orig.png" alt="drawing" width="600"/>

When users open the app, the app will send request to the server and obtain the information of the first pre-processed image. The original image and free space are both obtained as JPG format. The frames of vehicles and pedestrians as well as lane line curves are in JSON format. The JSON files will be parsed by the app and show on the canvas of the app.

2. Label images manually

<img src="https://charleszhu.weebly.com/uploads/8/3/6/2/83625186/capstone2_orig.png" alt="drawing" width="600"/>

The buttons on the fourth row are for free pace. The left is painting tool and the right is
eraser. When user clicks these two buttons, and all the frames will disappear and only the free space
will be shown.

<img src="https://charleszhu.weebly.com/uploads/8/3/6/2/83625186/capstone3_orig.png" alt="drawing" width="600"/>

The button on the last row is for lane lines. When the button is clicked, a quadratic Bezier
curve will be added. The curve is fitted with three points. The shape and location of the curve can
be adjusted by moving these three points. The start point of the curve will be set on the boundary of
the image automatically because it is where the lane line starts.

<img src="https://charleszhu.weebly.com/uploads/8/3/6/2/83625186/capstone-1_orig.png" alt="drawing" width="600"/>

The buttons at first two rows can add frames to vehicles, pedestrians and riders. The difference
of them is the type of frame they will set as “car”, “person” or “rider”. But their operation
method is the same. When user clicks the button, a rectangle will appear on the screen. User can
adjust its size and location. By single clicking inside the rectangle, the rectangle will change its
mode as “resize” or “replace”. When it is in one mode, users can only adjust either its size or its location
and the other mode is disabled. When it is in “resize” mode, the left and upper edges are
fixed and the right and lower ones are movable. Different colors are used on the frames to indicate
its type and which mode it’s in: yellow for “car”, green for “person”, purple for “”rider”, red for
“resize” and blue for “replace”(which is not shown on the figure because only allow one mode at a
time).

3. Upload results
When users complete labeling one image, the app can convert frames and lane line curves as
JSON and upload the modified version to back end and then obtain the next image.

## Development Environment & How to deploy

* IDE: Android Studio
* SDK: 

## Contact Information
