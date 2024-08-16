# Pock Pock - Chicken madness

A game I made in 2014 for my daughters (and to play around with some Java).

To run the game just use the following command in terminal:
    java -jar PockPock.jar

## How to compile, create JAR, and run the game

Compile java source code (from here):

    javac net/frakturmedia/pockpock/PockPock.java

Create JAR (java archive) from within the classes directory:

    jar vcfm PockPock.jar Manifest.txt net/frakturmedia/pockpock/*.class net/frakturmedia/pockpock/resources

Run PockPock.jar:

    java -jar PockPock.jar

