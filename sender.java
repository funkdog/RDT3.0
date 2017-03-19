// Michelle Emamdie
// CNT 4007C - Network Fundamentals
// Programming Assignment 2 - RDT 3.0

import java.io.*;
import java.net.*;

public class sender {
  String newline = System.getProperty("line.separator");
  Socket socket              = null;
  PrintWriter writer         = null;
  DataInputStream data       = null;
  BufferedReader buffer      = null;
  FileInputStream stream     = null;
  BufferedReader bufferInput = null;

  // Constructor that makes a valid connection to a port
  public sender(String hostName, int portNumber, String file) throws IOException {
    try {
      this.socket = new Socket(hostName, portNumber);
      this.writer = new PrintWriter(socket.getOutputStream(), true);
      this.bufferInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.stream = new FileInputStream(file);
      this.data = new DataInputStream(this.stream);
      this.buffer = new BufferedReader(new InputStreamReader(this.data));
    }
    catch (UnknownHostException e) {
     System.err.println("Cannot find the host: " + hostName);
     System.exit(1);
   } catch (IOException e) {
     System.err.println(e.getMessage());
     System.exit(1);
   } catch (Exception e) {
     System.err.println(e.getMessage());
   }
  }

  // Actually sends the packets. Handles dropping packets and corrupt packets
  public void sendPackets() throws IOException {
    String acknowledgement, input, message;
    int totalMessagesSent = 0;
    int i;
    //
    Packets packets = new Packets();
    try {
      input = (buffer.readLine());
      while (input != null) {
        System.out.println(input);
        String[] temp  = input.trim().split("\\s+");

        String[] split = new String[temp.length + 1];
        for (int j = 0; j < temp.length; j++) {
          split[j] = temp[j];
        }
        split[temp.length] = newline;
        i = 0;
        while(i < split.length) {
          packets.createPacket(split[i]);
          message = packets.generateMessage();
          writer.println(message);
          acknowledgement = bufferInput.readLine();
          System.out.println("Acknowledgement is: " + acknowledgement);
          totalMessagesSent++;
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
        input = (buffer.readLine());
      }
    }
    catch (EOFException e) {
      writer.println(-1);
      data.close();
    }

  }

  public void closeAll() throws IOException {
      this.writer.close();
      this.bufferInput.close();
      this.socket.close();
  }


  public static void main(String[] args) {
    if (args.length == 3) {
      String hostName = args[0];
      int portNumber = Integer.parseInt(args[1]);
      String fileName = args[2];

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
    else {
      System.err.println("Usage: java sender [URL] [Port Number] [Message File Name]");
      System.exit(1);
    }
  }
}
