# Simple DFS in Java with Jade

Simple Java program that implements a Distributed File System (DFS) using the Jade framework. The file system allows reading and writing files in the DFS. This program was made for the course "Distributed Systems & Real Time" of the Bachelor's degree in Computer Science at the National University of La Plata.

### Containers created:
* The DataServer is the one that stores the DFS information, there can be several, distributing the storage among different computers, just as real DFS do.
* The ServerDFS is the server that handles the client's requests regarding the file system, it is the one that receives the request to read or write a file.
* The Client is the one that makes the request to be made (reading or writing), it is the one that executes the agent for the first time.

Reading
Writing
reading with copy

### Run example
To run the experiment we have built the Bash script `dfs.sh` which receives two parameters, the operation to perform and the name of the file to read/write. Here some examples:
- Reading
  ```bash
  $ dfs -r sweet.mp3
  ```
- Writing
  ```bash
  $ dfs -w nuevo_archivo.txt
  ```
- Reading with copy
  ```bash
  $ dfs -r sweet.mp3 -c
  ```
