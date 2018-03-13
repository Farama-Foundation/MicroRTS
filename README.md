## microrts

microRTS is a small implementation of an RTS game, designed to perform AI research. The advantage of using microRTS with respect to using a full-fledged game like Wargus or Starcraft (using BWAPI) is that microRTS is much simpler, and can be used to quickly test theoretical ideas, before moving on to full-fledged RTS games.

microRTS is deterministic and real-time (i.e. players can issue actions simultaneously, and actions are durative). It is possible to experiment both with fully-observable and partially-observable games. Thus, it is not adequate for evaluating AI techniques designed to deal with non-determinism (although future versions of microRTS might include non-determinism activated via certain flags). As part of the implementation, I include a collection of hard-coded, and game-tree search techniques (such as variants of minimax, Monte Carlo search, and Monte Carlo Tree Search).

microRTS was developed by [Santiago Ontañón](https://sites.google.com/site/santiagoontanonvillar/Home). 

For a video of how microRTS looks like when a human plays see a [youtube video](https://www.youtube.com/watch?v=ZsKKAoiD7B0)

An AI competition was organized aroung microRTS in the IEEE-CIG 2017 conference, and again in 2018. For more information on the competition see the [competition website](https://sites.google.com/site/micrortsaicompetition/home)

To cite microRTS, please cite this paper:

Santiago Ontañón (2013) The Combinatorial Multi-Armed Bandit Problem and its Application to Real-Time Strategy Games, In AIIDE 2013. pp. 58 - 64.

## Contributions:

The LSI AI was contributed by Alexander Shleyfman, Antonin Komenda and Carmel Domshlak (the theory behind the AI is described in this [paper](https://www.researchgate.net/publication/282075129_On_Combinatorial_Actions_and_CMABs_with_Linear_Side_Information).

## Instructions:

![instructions image](https://raw.githubusercontent.com/santiontanon/microrts/master/help.png)
