[![Build Status](https://travis-ci.org/HBRS-MAAS/jade-tutorials.svg?branch=master)](https://travis-ci.org/HBRS-MAAS/jade-tutorials)

# Jade Tutorials

Make sure to keep this README updated, particularly on how to run your project from the commandline. Start the application by help of the jar file . Switch into project folder "ws18-jade-tutorials-janl1992" and run the command: 

"java -jar ws18-jade-tutorials-janl1992.jar"


## Dependencies
* JADE v.4.5.0
* Java 8
* Gradle

## How to run
Just install gradle and run:

    gradle run

It will automatically get the dependencies and start JADE with the configured agents.
In case you want to clean you workspace run

    gradle clean

## Eclipse
To use this project with eclipse run

    gradle eclipse

This command will create the necessary eclipse files.
Afterwards you can import the project folder.
