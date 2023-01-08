#  Activity 2

## Description
This program is a game that uses the protobuf as protocol. The game is simple because all it does is remove tiles from an image. The game works as a battleship board that uses 3 different possible boards each time the game is played one of them is randomly chosen to be played. All the player has to do is hit all of targets to win the game. The game works using a client and server interface. The given protobuf files are used to implement the game. The user is also ale to see the leaderboard and whenever they win they add a win to their name on the leaderboard.

## Requirements Met
	1. The project needs to run through Gradle.
	2. You need to implement the given protocol (Protobuf) see the
	Protobuf files, the README and the Protobuf example in the examples
	repo (this is NOT an optional requirement). 
	3. The main menu gives the user 3 options: 1: leaderboard, 2
	play game, 3 quit. After a game is done the menu should pop up again.
	Implement the menu on the Client side (not optional).
	4. When the user chooses the option 1, a leader board will be shown (does
	not have to be sorted or pretty).
	5. The leader board is the same for all users, take care of multiple users not
	overwriting it wrong and the leader board persists even if the server crashes and is
	re-started.
	6. User chooses option 2 (the game) a new image/text (with xxxx is sent to
	the client).
	7. Multiple users can enter the SAME game and will thus reveal the image
	faster.
	8. Users win when finishing an image and get back to main menu, multiple
	clients can win together (see video).
	9. Server sends a task and can check if it is done correctly (this needs to be
	on the server side).
	10. Client presents the information well and the task are small and fast to
	answer (one word answers).
	11. Game quits gracefully when option 3 is chosen.
	12. Server does not crash when the client just disconnect (without choosing
	option 3).
	13. Good and fun tasks, that are not the same the whole time and you
	randomly choose or some other fun way.
	14. You need to run and keep your server running on your AWS instance (or
	somewhere where others can reach it and test it) – if you used the protocol correctly
	everyone should be able to connect with their client. Keep a log of who logs onto
	your server (this is already included). I will give it 3 tries over a couple of days, if
	I can make it through a game and have at least two clients running on it you will
	get these points (or if someone else was able to do it). You will need to post your
	exact gradle command we can use (including ip and port) on the channel on Slack
	#servers.
	15. Your game has a good setup for how many tiles each task reveals, some
	kind of dependency on how many games someone won, or how often they logged in.
	You need to explain this in your Readme and we need to be able to experience it.
	Do not just turn one tile, an image should be revealed after at least 8 tasks.
	16. You test at least 3 other servers with your client and should show up on
	their log file. You should comment on their servers on Slack, this is how we will
	grade these two points. – this can be done up until 2 days after the official due date.
	17. In my version the board on the client is only updated after they made a
	"guess", change the implementation so that as soon as the server updates the board
	the client will get this information and will update the board and inform the user,
	e.g. client B makes a correct guess, so the board updates and all other clients will
	get the information about the new board right away.
	18. If user types in "exit" while in the game (instead of row or column), the
	client will exit gracefully.
	19. Overall your server/client programs do not crash, handle errors well and are well
	structured. No extra points for this but you might loose up to 5% if this is not the
	case since your pograms should be robust at this point

## How to run it 
The proto file can be compiled using

``gradle generateProto``

This will also be done when building the project. 

You should see the compiled proto file in Java under build/generated/source/proto/main/java/buffers

Now you can run the client and server. Make sure to be in the activity 1 folder and run the server first before the client is run.
```
cd Assignment4/activity2/ 
```
```
gradle runServer
```
```
gradle runClient
```
#### With parameters:

```
gradle runClient -Pport=9099 -Phost='localhost'
```
```
gradle runServer -Pport=9099
```

## Screencast:
https://www.youtube.com/watch?v=GfcXz1HOJ_Q
