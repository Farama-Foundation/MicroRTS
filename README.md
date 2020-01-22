# microRTS [![Build Status](https://travis-ci.org/douglasrizzo/microrts.svg?branch=master)](https://travis-ci.org/douglasrizzo/microrts)

microRTS is a small implementation of an RTS game, designed to perform AI research. The advantage of using microRTS with respect to using a full-fledged game like Wargus or StarCraft (using BWAPI) is that microRTS is much simpler, and can be used to quickly test theoretical ideas, before moving on to full-fledged RTS games.

microRTS is deterministic and real-time (i.e. players can issue actions simultaneously, and actions are durative). It is possible to experiment both with fully-observable and partially-observable games. Thus, it is not adequate for evaluating AI techniques designed to deal with non-determinism (although future versions of microRTS might include non-determinism activated via certain flags). As part of the implementation, I include a collection of hard-coded, and game-tree search techniques (such as variants of minimax, Monte Carlo search, and Monte Carlo Tree Search).

microRTS was developed by [Santiago Ontañón](https://sites.google.com/site/santiagoontanonvillar/Home). 

For a video of how microRTS looks like when a human plays, see a [YouTube video](https://www.youtube.com/watch?v=ZsKKAoiD7B0)

If you are interested in testing your algorithms against other people's, **there is an annual microRTS competition**. For more information on the competition see the [competition website](https://sites.google.com/site/micrortsaicompetition/home). The previous competitions have been organized at IEEE-CIG2017 and IEEE-CIG2018, and this year it's organized at IEEE-COG2019 (notice the change of name of the conference).

To cite microRTS, please cite this paper:

Santiago Ontañón (2013) The Combinatorial Multi-Armed Bandit Problem and its Application to Real-Time Strategy Games, In AIIDE 2013. pp. 58 - 64.

## Setting up microRTS in an IDE

Watch [this YouTube video](https://www.youtube.com/watch?v=_jVOMNqw3Qs) to learn how to acquire microRTS and setup a project using Netbeans.

## Executing microRTS through the terminal

If you want to build and run microRTS from source using the command line, clone or download this repository and run the following commands in the root folder of the project to compile the source code:

Linux or Mac OS:

```shell
javac -cp "lib/*:src" -d bin src/rts/MicroRTS.java # to build
```

Windows:

```shell
javac -cp "lib/*;src" -d bin src/rts/MicroRTS.java # to build
```

### Generating a JAR file

You can join all compiled source files and dependencies into a single JAR file, which can be executed on its own. In order to create a JAR file for microRTS:

```shell
javac -cp "lib/*:src" -d bin $(find . -name "*.java") # compile source files
cd bin
find ../lib -name "*.jar" | xargs -n 1 jar xvf # extract the contents of the JAR dependencies
jar cvf microrts.jar $(find . -name '*.class' -type f) # create a single JAR file with sources and dependencies
```

### Executing microRTS

To execute microRTS from compiled class files:

```shell
java -cp "lib/*:bin" rts.MicroRTS # on Linux/Mac
java -cp "lib/*;bin" rts.MicroRTS # on Windows
```

To execute microRTS from the JAR file:

```shell
java -cp microrts.jar rts.MicroRTS
```

#### Which class to execute

microRTS has multiple entry points, and for experimentation purposes you might eventually want to create your own class if none of the base ones suit your needs (see the "tests" folder for examples), but a default one is the `gui.frontend.FrontEnd` class, which opens the default GUI. To execute microRTS in this way, use the following command:

```shell
java -cp microrts.jar gui.frontend.FrontEnd
```

Another, more expansive entry point is the `rts.MicroRTS` class. It is capable of starting microRTS in multiple modes, such as in client mode (attempts to connect to a server which will provide commands to a bot), server mode (tries to connect to a client in order to control a bot), run a standalone game and exit or open the default GUI.

The `rts.MicroRTS` class accepts multiple initialization parameters, either from the command line or from a properties file. A list of all the acceptable command-line arguments can be accessed through the following command:

```shell
java -cp microrts.jar rts.MicroRTS -h
```

An example of a properties file is provided in the `resources` directory. microRTS can be started using a properties file with the following command:

```shell
java -cp microrts.jar rts.MicroRTS -f my_file.properties
```

## Instructions

![instructions image](https://raw.githubusercontent.com/santiontanon/microrts/master/help.png)
