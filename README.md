# iRACER: FPV Android Racing Game with real RC Cars

This project was started in 2018 and is not associated with the PC simulation game. It is no longer an active project but it still works very well.

## Video:
[![Introducing IRACER](https://img.youtube.com/vi/e1PcXD0_e2s/0.jpg)](https://www.youtube.com/watch?v=e1PcXD0_e2s)

## Background and technical details

The protopype cars used an inexpensive RC car chassis with a Raspberry Pi Zero W and a camera.  A color sensor and LED light under the car was used to detect colored mats that represented the start and finish lines as well as Mario Kart inspired power-ups.  

This Android app provides the FPV view, control of the car, and multiplayer game client/server. This could be a fun project to recreate or use as a starting point for controlling other RC vehicles with Android app and FPV view.  The video feed is very low latency (gstreamer over UDP with much trial and error).

"Mario Kart Live: Home Circuit" was announced by Nintendo in 2020 and released in 2021.  It uses a similar concept of using a camera on an RC car to provide an augmented reality experience.

## Getting started
The APK can be found here:
IRACER-Android\android-tutorial-3\release\android-tutorial-3-release.apk
(yes I would refactor this if it were still an active project.  Development started from the Android gstreamer sample project)

See https://github.com/spencershepard/iracer-firmware for the python script that controls the servo, ESC, and camera stream.