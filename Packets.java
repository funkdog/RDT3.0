// Michelle Emamdie
// CNT 4007C - Network Fundamentals
// Programming Assignment 2 - RDT 3.0

import java.io.*;
import java.net.*;

public class Packets {
  int sequenceNum, packetID, checkSum;
  String content;
  boolean last;
  String newline = System.getProperty("line.separator");

  public Packets() {
    this.sequenceNum = 1;
    this.packetID    = 0;
    this.last        = false;
  }

  public void createPacket(String content) {
    this.content = content;

    if (this.sequenceNum == 0) {
      this.sequenceNum = 1;
    }
    else {
      this.sequenceNum = 0;
    }

    this.checkSum = generateChecksum(this.content);

    this.packetID++;
  }

  public int generateChecksum(String content) {
    int ascii = 0;
    int sum = 0;
    for (int i = 0; i < content.length() ; i++ ) {
      ascii = (int) content.charAt(i);
      sum += ascii;
      if (ascii == 46) {
        last = true;
      }
    }
    return sum;
  }

  public boolean validateAck(String ack) {
      return ack.equals("ACK" + Integer.toString(sequenceNum));
  }

  public String validate() {
    Integer newcs = generateChecksum(content);
    if (newcs.equals(checkSum)) {
      return "ACK" + Integer.toString(sequenceNum);
    }
    else {
      if (sequenceNum == 0) {
        sequenceNum = 1;
      }
      else {
        sequenceNum = 0;
      }
      return "ACK" + Integer.toString(sequenceNum);
    }
  }

  public void parse(String input) {
    String[] split = input.split("\\s+");
    sequenceNum = Integer.parseInt(split[0]);
    packetID = Integer.parseInt(split[1]);
    checkSum = Integer.parseInt(split[2]);
    if (split.length == 4) {
      content = split[3];
    } else {
      content = newline;
    }
  }

  public String generateMessage() {
    return sequenceNum + " " + packetID + " " + checkSum + " " + content;
  }
}
