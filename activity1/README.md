#  Activity 1
## Description
This program includes a single-threader server and a multi-threaded server. This also has 2 versions, one that allows for the threads to grow unbounded and another that specifies the number of clients allowed. This program runs as a Client-Server interface where the 3 different tasks are the servers that can be ran for each task and the client is the same one used to run all 3 tasks. The client of the program is given 6 options when runnning the program. They are able to add a string to the list, remove an item/string from the list, display all of the items that are currently in the list, retrieve the number of elements that are in the list, reversing the string so that it is a reversal of the original string entered, and finally quitting the program.  

### Requests
request: { "selected": <int: 1=add, 2=remove, 3=display, 4=count, 5=reverse,
0=quit>, "data": <thing to send>}

add: data <string> | add string to list\
remove: data <int> | remove string from list\
display: no data | displays all elements in the list\
count: no data | counts the number of elements in the list\
reverse: data <int> | reverses a string in the list\
quit: no data

### Responses

success response: {"type": <"add",
"remove", "display", "count", "reverse", "quit"> "data": <thing to return> }\

type <String>: echoes original selected from request (e.g. if request was "add", type will be "add")
data <string>: add = new list\ 
remove = removed element\
display = current list\ 
count = num elements\
reverse = new list\

error response: {"type": "error", "message"": <error string> }
Gives good error message if something goes wrong which explains what went wrong

## How to run the program
Task 1, please use the following commands:
```
    cd Assignment4/activity1/
```
```
    For Server, run "gradle runTask1"
```    
```   
    For Client, run "gradle runClient"
``` 

Task 2, please use the following commands:
```
    cd Assignment4/activity1/
```
```
    For Server, run "gradle runTask2"
```
```   
    For Client, run "gradle runClient"
```

Task 3, please use the following commands:
```
    cd Assignment4/activity1/
```
```
    For Server, run "gradle runTask3"
```
```   
    For Client, run "gradle runClient"
```
## Requirements Met
### Task 1
Functionality to remove an element from the list.\
Functionality to add an element to the list.\    
Functionality to display content of the list.\
Functionality to count the number of elements in the list.\
Functionality to reverse an elements order.\
Functionality to quit the program.
### Task 2
Functionality for unbounded multi-threaded server with no client blocking.\
Shared data is managed via synchronous methods.
### Task 3
Functionality for bounded (n) multi-threaded server.
    
## Screencast:
https://www.youtube.com/watch?v=OYNPMtlj7Kw
    
    
