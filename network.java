// Michelle Emamdie
// CNT 4007C - Network Fundamentals
// Programming Assignment 2 - RDT 3.0

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Class: network
 * Functions:
 *      main - Creates a socket on the server on the designated port and then waits for a receiver and sender to join.
 * Properties:
 *      ArrayList threads - Array of MessageThreads containing the open threads on the network
 *      server - The location where members who join the network will connect to.
 * Static Classes:
 *      MessageThread extends Thread
 */
public class network {
  /**
   * Array of MessageThreads containing the open threads on the network
   */
  static ArrayList<MessageThread> threads = new ArrayList<MessageThread>();
  /**
   * The location where members who join the network will connect to.
   */
  static ServerSocket server;

  /**
   * Creates a socket on the server on the designated port and then waits for a receiver and sender to join.
   * @param args Port number to create a network on.
   */
  public static void main(String[] args) {
    // Verify that the user has entered the proper number of arguments when creating an instance of the class.
    if (args.length == 1) {
      int portNumber = Integer.parseInt(args[0]);
      // Try to create a server socket for others to connect to
      try {
        server = new ServerSocket(portNumber);
        System.out.println("Waiting... connect receiver");
        // Start threads for the receiver and the sender to connect to
        MessageThread rec = new MessageThread(server.accept());
        MessageThread sen = new MessageThread(server.accept());
        rec.start();
        sen.start();
      }
      // If a server socket cannot be created catch the error and terminate gracefully
      catch (IOException e) {
        System.err.println(e.getMessage());
        e.printStackTrace();
      }
    }
    // The user did not enter the proper number of arguments
    else {
      System.err.println("Usage: java network [Port Number]");
      System.exit(1);
    }
  }

  /**
   * Helper class that spins off new threads so that sender and receiver run on isolated threads but the same network
   * Properties:
   *    String ACK2 - constant
   *    String newline - constant
   *    Socket socket - The socket that the network has opened
   *    int id - used to identify which thread this is
   * Functions:
   *    Constructor - creates a new Message Thread
   *    run() - Will be the driving force to allow the sender and the receiver to communicate.
   *    send(String) - Sends a messasge to the output socket
   *    toDifferentThread(String) - Sends message to the other thread executing on the network
   */
  public static class MessageThread extends Thread {
    /**
     * The string to compare to later on.
     */
    final String ACK2 = "ACK2";
    /**
     * New line character to be used when the end of the line has been located.
     */
    String newline = System.getProperty("line.separator");
    /**
     * Connects this thread to the socket currently running on the server
     */
    private Socket socket = null;
    /**
     * Used to count the number of message threads open
     */
    int id = 0;

    /**
     * @param socket The socket which the thread will be connected to on the server.
     */
    public MessageThread(Socket socket) {
      this.socket = socket;
      threads.add(this);
      this.id = threads.size() - 1;
    }

    /**
     * Will be the driving force to allow the sender and the receiver to communicate.
     * PrintWriter is used to write to the receiver.
     * BufferedReader is used to read the output of the sender.
     * @throws NullPointerException If the EOF has been reached
     */
    public void run() throws NullPointerException{
      try {
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Get connection from: " + socket.getRemoteSocketAddress().toString());
        String input;
        while ((input = reader.readLine()) != null) {
          // Check to see if we've received an exit command
          if (input.equals("-1") && id == 1) {
            toDifferentThread(input);
          }
          if (input.equals("-1")) {
            break;
          }
          // Ensure that the input is not an empty string
          // Inputs should be in packet form when they come into the network
          if(input.isEmpty()) {
            input = reader.readLine();
          }
          // Remove spaces from the packet
          String[] split = input.split("\\s+");
          // Generate a random number between 0 and 1
          double randomNum = Math.random();

          if (randomNum < 0.5 || split.length == 1) {
            //Randomly accept the packet
            String logMessage = "Received: ";
            if (!split[0].contains("ACK")) {
              logMessage += "ACK";
            }
            System.out.println(logMessage + split[0] + ", PASS");
            // Send the packet to the other thread from the one you are currently on.
            toDifferentThread(input);
          } else if (randomNum >= 0.5 && randomNum <= 0.75) {
            // Randomly corrupt the packet
            Packets packet = new Packets();
            packet.parse(input);
            //Corrupts the checksum
            packet.checkSum++;
            System.out.println("Received: Packet" + split[0] + ", " + split[1] + ", CORRUPT");
            // Make a new packet after corrupting this one
            toDifferentThread(packet.generateMessage());
          } else {
            // Randomly drop a packet
            System.out.println("Received: Packet" + split[0] + ", " + split[1] + ", DROP");
            writer.println(ACK2);
          }
        }
        // Close the socket once all packets ahve been sent and received
        socket.close();
      }
      // Catch IO Exceptions generated from the writers and readers
      catch (IOException e) {
        e.printStackTrace();
      }
    }

    /**
     * Sends a messasge to the output socket
     * @param message The message to send to the output stream
     */
    public void send(String message) {
      try {
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        writer.println(message);
      }
      catch(IOException e) {
        e.printStackTrace();
      }
    }

    /**
     * Sends message to the other thread executing on the network
     * @param message The message to be sent
     */
    public void toDifferentThread(String message) {
      if (id == 0) {
        ((MessageThread) threads.get(1)).send(message);
      }
      else {
        ((MessageThread) threads.get(0)).send(message);
      }
    }
  }
}
