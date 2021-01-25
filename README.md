Reshifter
=========

Detects and corrects for "blockstate mismatch" bugs when connecting to a server.

Blockstates that exist on the server but not the client will be stripped.  
Blockstates that exist on the client but not the server will simply never appear.

LGPL 3.0 or later.

## Problem Statement

The client and server independently compute their own `Blocks.STATE_IDS` table, and *assume* it always holds the same contents. Clients assume servers have an identical table, servers assume clients have an identical table. This table is consulted for serializing and deserializing chunk packets (among other things), because numbers are a lot cheaper to send over the network than full-blown stringified state IDs.

Usually, the assumption holds and the table *happens* to be identical: if the list of block IDs is the same, the list of blockstates is also the same. However it's not always the case if there are mods that hack additional blockstates onto vanilla blocks, such as Better End adding new features to chorus plants, and old versions of Trees Do Not Float adding a blockstate to detect player-placed logs.

If a mod that does this is present on only *one* of the client *or* the server, you get very weird-looking results ingame where most blocks look OK but many blockstates are replaced with seemingly random other blockstates.

## Algorithm Description

(both for my own reference and for onlookers)

#### Server Hello

Server computes and sends the hash of its `STATE_IDS` table.

#### Client Acknowledgement

Client compares the received hash to its own.