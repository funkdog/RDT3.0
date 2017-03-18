// Michelle Emamdie
// CNT 4007C - Network Fundamentals
// Programming Assignment 2 - RDT 3.0

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class network {
  static ArrayList<Object> threads = new ArrayList<Object>();
  static ServerSocket server;

  public static void main(String[] args) {
    if (args.length == 1) {
      int portNumber = Integer.parseInt(args[0]);
      try {
        server = new ServerSocket(portNumber);
        System.out.println("Waiting... connect receiver");
        new MessageThread(server.accept()).start();
      }
      catch (Exception e) {
        System.err.println(e.getMessage());
        e.printStackTrace();
      }
    }
    else {
      System.err.println("Usage: java network [Port Number]");
      System.exit(1);
    }
  }

  public static class MessageThread extends Thread {
    final String ACK0 = "ACK0";
    final String ACK1 = "ACK1";
    final String ACK2 = "ACK2";

    private Socket socket = null;
    MessageThread messageThread = null;
    int id = 0;

    public MessageThread(Socket socket) {
      this.socket = socket;
      threads.add(this);
      this.id = threads.size() - 1;
    }

    public void run() {
      try {
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Get connection from: " + socket.getRemoteSocketAddress().toString());
        String input = reader.readLine();
        while (input != null) {
          if (input.equals("-1") && id == 1) {
            toDifferentThread(input);
          }
          if (input.equals("-1")) {
            break;
          }
          String[] split = input.split("\\s+");
          double randomNum = Math.random();

          if (randomNum < 0.5 || split.length == 1) {
            String logMessage = "Received: ";
            if (!split[0].contains("ACK")) {
              logMessage += "ACK";
            }
            System.out.println(logMessage + split[0] + ", PASS");
            toDifferentThread(input);
          } else if (randomNum >= 0.5 && randomNum <= 0.75) {
            Packets packet = new Packets();
            packet.parse(input);
            //Corrupts the checksum
            packet.checkSum++;
            System.out.println("Received: Packet" + split[0] + ", " + split[1] + ", CORRUPT");
            toDifferentThread(packet.generateMessage());
          } else {
            System.out.println("Received: Packet" + split[0] + ", " + split[1] + ", DROP");
            writer.println(ACK2);
          }
          input = reader.readLine();
        }
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public void send(String message) {
      try {
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        writer.println(message);
      }
      catch(IOException e) {
        e.printStackTrace();
      }
    }

    public void toDifferentThread(String message) {
      if (id == 0) {
        messageThread = (MessageThread) threads.get(1);
      }
      else {
        messageThread = (MessageThread) threads.get(0);
      }
      messageThread.send(message);
    }
  }
}
