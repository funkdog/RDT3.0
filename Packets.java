// Michelle Emamdie
// CNT 4007C - Network Fundamentals
// Programming Assignment 2 - RDT 3.0

import java.io.*;
import java.net.*;

/**
 * Class: Packets
 *
 * Functions:
 *    Constructor - Initialize the properties
 *    createPacket(String) - Creates a new packet with the content passed to it
 *    generateChecksum(String) - Counts the sum of the ascii values in the content string
 *    validateAck(String) - Determines if the acknowledgement received is valid
 *    validate() - Validates the checksum to make sure it has not changed from what it should be
 *    parse(String) - Take the string and set the properties for a packet
 *    generateMessage() - Takes the packet and turns it into as string so that it can be sent to the network
 * Properties:
 *    int sequenceNum - alternating 0 or 1 for each packet
 *    int packetID - the current number of packets
 *    int checkSum - sum of the ascii character values of the string
 *    String content - The data that will be part of the message
 *    boolean last - True when the last packet is being sent
 */
public class Packets {
  /**
   * sequenceNum - alternating 0 or 1 for each packet
   * packetID - the current number of packets
   * checkSum - sum of the ascii character values of the string
   */
  int sequenceNum, packetID, checkSum;
  /**
   * The data that will be part of the message
   */
  String content;
  /**
   * True when the last packet is being sent
   */
  boolean last;
  /**
   * Constructor to initialize the properties
   */
  public Packets() {
    this.sequenceNum = 1;
    this.packetID    = 0;
    this.last        = false;
  }

  /**
   * Creates a new packet with the content passed to it
   * @param content The data that the packet should hold
   */
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

  /**
   * Counts the sum of the ascii values in the content string
   * @param content The data part of the string
   * @return the total sum
   */
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

  /**
   * Determines if the acknowledgement received is valid
   * @param ack The acknowledgement to check
   * @return True if the acknowledgement is valid
   */
  public boolean validateAck(String ack) {
      return ack.equals("ACK" + Integer.toString(sequenceNum));
  }

  /**
   * Validates the checksum to make sure it has not changed from what it should be
   * @return The acknowledgement based on the validity of the checksum
   */
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

  /**
   * Take the string and set the properties for a packet
   * @param input The message sent to the caller
   */
  public void parse(String input) {
    String[] split = input.split("\\s+");
    sequenceNum = Integer.parseInt(split[0]);
    packetID = Integer.parseInt(split[1]);
    checkSum = Integer.parseInt(split[2]);
    content = split[3];

  }

  /**
   * Takes the packet and turns it into as string so that it can be sent to the network
   * @return String to be sent to the network
   */
  public String generateMessage() {
    return sequenceNum + " " + packetID + " " + checkSum + " " + content;
  }
}
