# microrts

microRTS is a very simple Java implementation of an RTS game designed to test AI techniques. The motivation to create microRTS was to be able to test simple AI techniques without having to invest the high development time that is required to start working with Wargus or Starcraft using BWAPI. Also, for some AI techniques, one needs to know the exact details of the transition function used in the game, which is not available for some of those games.

microRTS is deterministic, fully-observable and real-time (i.e. players can issue actions simultaneously, and actions are durative). For that reason, it is not adequate for evaluating techniques designed to address non-determinism or partial observability. I created it for testing, in particular, game-tree search techniques such as Monte Carlo search algorithms.

Although microRTS is designed to run without the need for a visualization (since it is not meant to be for a human to play, but for AIs), it comes with a simple visualization panel that can be used to see games in real-time (see the wiki pages for a screenshot).

Maps can be defined directly in code, or using xml files.
