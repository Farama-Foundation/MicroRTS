# microRTS [![Build Status](https://travis-ci.org/douglasrizzo/microrts.svg?branch=master)](https://travis-ci.org/douglasrizzo/microrts)

microRTS is a small implementation of an RTS game, designed to perform AI research. The advantage of using microRTS with respect to using a full-fledged game like Wargus or StarCraft (using BWAPI) is that microRTS is much simpler, and can be used to quickly test theoretical ideas, before moving on to full-fledged RTS games.

microRTS is deterministic and real-time (i.e. players can issue actions simultaneously, and actions are durative). It is possible to experiment both with fully-observable and partially-observable games. Thus, it is not adequate for evaluating AI techniques designed to deal with non-determinism (although future versions of microRTS might include non-determinism activated via certain flags). As part of the implementation, I include a collection of hard-coded, and game-tree search techniques (such as variants of minimax, Monte Carlo search, and Monte Carlo Tree Search).

microRTS was developed by [Santiago Ontañón](https://sites.google.com/site/santiagoontanonvillar/Home). 

For a video of how microRTS looks like when a human plays, see a [YouTube video](https://www.youtube.com/watch?v=ZsKKAoiD7B0)

If you are interested in testing your algorithms against other people's, **there is an annual microRTS competition**. For more information on the competition see the [competition website](https://sites.google.com/site/micrortsaicompetition/home). The previous competitions have been organized at IEEE-CIG2017 and IEEE-CIG2018, and this year it's organized at IEEE-COG2019 (notice the change of name of the conference).

To cite microRTS, please cite this paper:

Santiago Ontañón (2013) The Combinatorial Multi-Armed Bandit Problem and its Application to Real-Time Strategy Games, In AIIDE 2013. pp. 58 - 64.

## Setting up microRTS

Watch [this YouTube video](https://www.youtube.com/watch?v=_jVOMNqw3Qs) to learn how to acquire microRTS and setup a project using Netbeans.

### Terminal commands

If you want to build and run microRTS using the Linux or Mac OS command line, clone or download this repository and run the following commands in the root folder of the project:

```shell
javac -cp "lib/*:src" -d bin src/rts/MicroRTS.java # to build
java -cp "lib/*:bin" rts.MicroRTS # to run
```

Or in Windows:

```shell
javac -cp "lib/*;src" -d bin src/rts/MicroRTS.java # to build
java -cp "lib/*;bin" rts.MicroRTS # to run
```

If you want to run other classes that have a `main` method (such as `gui/frontend/FrontEnd.java`), change the build and run commands accordingly.

## Instructions

![instructions image](https://raw.githubusercontent.com/santiontanon/microrts/master/help.png)
