// Michelle Emamdie
// CNT 4007C - Network Fundamentals
// Programming Assignment 2 - RDT 3.0

import java.io.*;
import java.net.*;

/**
 * Class: sender
 *
 * Functions:
 *    Constructor - Makes a valid connection to a port
 *    sendPackets() - Creates packets that will be sent to the server and sends them to the server.
 *                    Also receives responses from the server
 *    closeAll() - Close all the open connections
 *    main() - Used to create a sender that will read a file and send the message as packets to the server
 *
 * Properties:
 *    Socket socket - Connects this thread to the socket currently running on the server
 *    PrintWriter writer - Used to output to the server socket so that messages can be sent to the receiver
 *    BufferedReader buffer - The input from the file
 *    BufferedReader bufferInput - The input from the socket
 *    DataInputStream data - The stream of data coming in from the file
 *    FileInputStream stream - Used to read the file from the directory
 */
class sender {
  /**
   * Connects this thread to the socket currently running on the server
   */
  private Socket socket              = null;
  /**
   * Used to output to the server socket so that messages can be sent to the receiver
   */
  private PrintWriter writer         = null;
  /**
   * The input from the file
   */
  private BufferedReader buffer      = null;
  /**
   * The input from the socket
   */
  private BufferedReader bufferInput = null;
  /**
  * The stream of data coming in from the file
  */
  private DataInputStream data       = null;

  /**
   * Constructor that makes a valid connection to a port
   * @param hostName Name of the server to connect to
   * @param portNumber Port on which to connect
   * @param file The file that contains the message to be sent
   */
  private sender(String hostName, int portNumber, String file) {
    try {
      this.socket = new Socket(hostName, portNumber);
      this.writer = new PrintWriter(socket.getOutputStream(), true);
      this.bufferInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      /*
   Used to read the file from the directory
  */
      FileInputStream stream = new FileInputStream(file);
      this.data = new DataInputStream(stream);
      this.buffer = new BufferedReader(new InputStreamReader(this.data));
    }
    catch (UnknownHostException e) {
     System.err.println("Cannot find the host: " + hostName);
     System.exit(1);
   } catch (Exception e) {
     System.err.println(e.getMessage());
   }
  }

  /**
   * Creates packets that will be sent to the server and sends them to the server.
   * Also receives responses from the server
   * @throws IOException Occurs if read or write do not work
   */
  // Actually sends the packets. Handles dropping packets and corrupt packets
  private void sendPackets() throws IOException {
    String acknowledgement, input, message;
    int totalMessagesSent = 0;
    int i;
    // Create a new packet object
    Packets packets = new Packets();
    try {
      while ((input = buffer.readLine()) != null) {
        // Trim leading and trailing white spaces then split based on spaces
        String[] split  = input.trim().split("\\s+");
        i = 0;
        while(i < split.length) {
          // Create a packet based on the split string
          packets.createPacket(split[i]);
          // From the packet, prepare a message to send that the server will understand
          message = packets.generateMessage();
          // Send the message to the server
          writer.println(message);
          acknowledgement = bufferInput.readLine();
          totalMessagesSent++;
          // Handle acknowledgement received
          if (acknowledgement.equals("ACK2")) {
              System.out.println("Waiting: " + acknowledgement + ", " + totalMessagesSent + ", DROP, resend packet" + packets.sequenceNum + ".");
          }
          else if (packets.validateAck(acknowledgement)) {
            i++;
            System.out.println("Waiting: " + acknowledgement + ", " + totalMessagesSent + ", " + acknowledgement + ", no more packets to send.");
          }
          else {
            System.out.println("Waiting: " + acknowledgement + ", " + totalMessagesSent + ", " + acknowledgement + ", send Packet " + packets.sequenceNum);
          }
        }
      }
      writer.println(-1);
      data.close();
    }
    // End of the file has been reached. Terminate gracefully
    catch (EOFException e) {
      writer.println(-1);
      data.close();
    }
  }

  /**
   * Close all the open connections
   * @throws IOException Thrown if the objects cannot close properly
   */
  private void closeAll() throws IOException {
      this.writer.close();
      this.bufferInput.close();
      this.socket.close();
  }


  /**
   * Used to create a sender that will read a file and send the message as packets to the server
   * @param args Host, port, and file name
   */
  public static void main(String[] args) {
    // Verify that there are three input arguments
    if (args.length == 3) {
      String hostName = args[0];
      int portNumber = Integer.parseInt(args[1]);
      String fileName = args[2];
      // Create a sender, send the packets, then close all open connections.
      try {
        sender s = new sender(hostName, portNumber, fileName);
        s.sendPackets();
        s.closeAll();
      }
      catch (Exception e) {
        System.err.println(e.getMessage());
        e.printStackTrace();
      }
    }
    // If there are not warn of the proper usage for this command
    else {
      System.err.println("Usage: java sender [URL] [Port Number] [Message File Name]");
      System.exit(1);
    }
  }
}
