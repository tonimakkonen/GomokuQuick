# Gomoku Quick

A simple Android app that can be used to play Gomoku againts the computer or a human opponent on the same device.
This project was quickly put together in early 2014, added to GitHub March 2017, and the code currently being cleaned up.

## Features

Gomoku Quick allows playing againts the CPU and againts a human opponent on the same phone.
Three set of rules are implemented: classic gomoku, caro (gomoku+), and 6-in-a-row.
In Gomoku, the first player to get 5-in-a-row wins.
In Caro, only free fives are counted.
As a curiosity, these three games can be played on a hexagonal board.

## AI

The gomoku AI is based on the minimax algorith with alpha-beta pruning.
It can offer a decent challenge with a short computation time on any modern Android phone.
Some heurestic rules are used to improve the AI computation time.
The minimax algorithm counts moves close to the last move and then moves next to marked tiles.

## Limitations

Some tweaking to the AI is still required, as it does not perform well on other games than classic Gomoku.