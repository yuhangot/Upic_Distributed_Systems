package com.upic.client;
public class Main {
  public static void main(String[] args) {
    SkierClient client = new SkierClient("http://18.237.105.173:8080/Upic_war/skiers");
    LiftRideEventGenerator generator = new LiftRideEventGenerator();
    String requestBody = generator.generateLiftRideEvent();

    try {
      client.sendPostRequest("skiers", requestBody);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
