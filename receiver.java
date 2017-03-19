// Michelle Emamdie
// CNT 4007C - Network Fundamentals
// Programming Assignment 2 - RDT 3.0

import java.io.*;
import java.net.*;

/**
 * Class: receiver
 *
 * Functions:
 *    Constructor-  Makes a receiver based on the host name and port number
 *    run()- Handles the reception of packets from the network server.
 *    closeAll()- Close out all the open connections
 *    main()- Takes in two inputs, the host and the port
 * Properties:
 *    Socket
 *    PrintWriter
 *    BufferedReader
 */
public class receiver {
  /**
   * The socket that will be used to connect to the server socket
   */
  Socket socket         = null;
  /**
   * Used to output to the server socket so that messages can be sent to the sender
   */
  PrintWriter writer    = null;
  /**
   * The input from the socket
   */
  BufferedReader buffer = null;

  /**
   * Constructor that makes a receiver based on the host name and port number
   * @param hostName host name for the socket to connect to
   * @param portNumber port number that the socket is running on
   * @throws IOException if host name cannot be reached or other fields cannot be created
   */
  public receiver(String hostName, int portNumber) throws IOException {
    try {
      this.socket = new Socket(hostName, portNumber);
      this.writer = new PrintWriter(socket.getOutputStream(), true);
      this.buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    catch (UnknownHostException e) {
     System.err.println("Cannot find the host: " + hostName);
     System.exit(1);
   }
  }

  /**
   * Handles the reception of packets from the network server.
   */
  public void run() {
    String input, output, message;
    int packetsReceived = 0;

    System.out.println("Waiting... connect sender");
    message = "";
    // Create a new packet
    Packets packet = new Packets();
    try {
      // Read until the input is empty.
      // It should never really be null because an exit command is sent first
      while((input = buffer.readLine()) != null) {
        while (input.isEmpty()) {
          input = buffer.readLine();
        }
        // Handle exit command
        if (input.equals("-1")) {
          writer.println(-1);
          break;
        }
        // Parse the packet since it is not an exit command
        packet.parse(input);
        // Concatenate packet content to existing message string
        message += packet.content + " ";
        // Count the number of packets received
        packetsReceived++;

        // Output the message as instructed in the program instructions
        output = "Waiting " + packet.sequenceNum + ", " + packetsReceived + ", " + input + ", " + packet.validate();
        System.out.println(output);
        // Make sure the check sum is valid
        writer.println(packet.validate());
        // If the last packet has been sent and if the last character is a period print the message
        if (packet.last && message.charAt(message.length()-2) == '.') {
          System.out.println("Message: " + message);
          message = "";
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   *  Close out all the open connections
   * @throws IOException
   */
  public void closeAll() throws IOException {
    this.writer.close();
    this.buffer.close();
    this.socket.close();
  }

  /**
   * @param args  Takes in two inputs, the host and the port
   */
  public static void main(String[] args) {
    // Verify that there are two inputs
    if (args.length == 2) {
      String hostName = args[0];
      int portNumber = Integer.parseInt(args[1]);
      // Create a new receiver object using the input parameters
      try {
        receiver rec = new receiver(hostName, portNumber);
        rec.run();
        rec.closeAll();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    else {
      System.err.println("Usage: java receiver [URL] [Port Number]");
      System.exit(1);
    }
  }
}
