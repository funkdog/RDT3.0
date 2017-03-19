// Michelle Emamdie
// CNT 4007C - Network Fundamentals
// Programming Assignment 2 - RDT 3.0

import java.io.*;
import java.net.*;

public class receiver {
  Socket socket         = null;
  PrintWriter writer    = null;
  BufferedReader buffer = null;

  public receiver(String hostName, int portNumber) throws IOException {
    try {
      this.socket = new Socket(hostName, portNumber);
      this.writer = new PrintWriter(socket.getOutputStream(), true);
      this.buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    catch (UnknownHostException e) {
     System.err.println("Cannot find the host: " + hostName);
     System.exit(1);
   } catch (IOException e) {
     System.err.println(e.getMessage());
     System.exit(1);
   }
  }

  public void run() {
    String input, output, message;
    int packetsReceived = 0;

    System.out.println("Waiting... connect sender");
    message = "";
    Packets packet = new Packets();
    try {
      while((input = buffer.readLine()) != null) {
        while (input.isEmpty()) {
          input = buffer.readLine();
        }
        if (input.equals("-1")) {
          writer.println(-1);
          break;
        }
        packet.parse(input);
        message += packet.content + " ";
        packetsReceived++;

        output = "Waiting " + packet.sequenceNum + ", " + packetsReceived + ", " + input + ", " + packet.validate();
        System.out.println(output);

        writer.println(packet.validate());
        if (packet.last && message.charAt(message.length()-2) == '.') {
          System.out.println("Message: " + message);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public void closeAll() throws IOException {
    this.writer.close();
    this.buffer.close();
    this.socket.close();
  }

  public static void main(String[] args) {
    if (args.length == 2) {
      String hostName = args[0];
      int portNumber = Integer.parseInt(args[1]);
      try {
        receiver rec = new receiver(hostName, portNumber);
        rec.run();
        rec.closeAll();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    else {

    }
  }
}
