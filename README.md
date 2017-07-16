# Surveillance Camera Android Application

This project is developed as my graduation thesis. Motivation for developing this application was, notify user when there is a change in an interested place. Project comprises two parts. First part is hardware part which includes a Raspberry Pi, a camera, an Arduino, motion and sound sensors. Second part is this application.
** Application still has bugs and contains some unnecessary codes for testing but it works as expected :)

## Use cases 

- System can be used as a surveillance system to notify user when sound and/or motion is detected.
- System can be used as a baby monitoring system. When baby cries, notifys parents.
In both scenerio, using the camera built to the system, users are able to watch real time status of the environment.

## Features

- Push notifications using MQTT protocol (configurable using Settings)
* Live stream support
* Autostart service after boot (configurable using Settings)
* Remember configurations

