# AnimatronicEyes
Software to controll animatronic eyes and connect them with AI.

## Purpose
The purpose of this project is to create a set of animatronic eyes that can be controlled from Java and have them be connected to AI for facial tracking.

## How to use
This project uses Maven to help with maneging libraries. Using the [Maven tool](https://maven.apache.org/download.cgi) run `clean install` to set up the project.

To run controller controlled eyes, a Java VM argument must be specified pointing jinput to the folder that contains its native components. Example: `-Djava.library.path=target/natives`
If you are using an IDE, you can also specify a natives directory for the jinput natives.

If you do not want to use Maven, you can download and add the jSerialComm library to your project manually. For the controller controlled eyes, jinput will also need to be manually added.

## Acknowledgement
### jSerialComm
Thanks to Fazecast, Inc. (Fazecast) for the [serial library](https://github.com/Fazecast/jSerialComm)
### jinput
Thanks to jinput for the [input device library](https://github.com/jinput/jinput)

