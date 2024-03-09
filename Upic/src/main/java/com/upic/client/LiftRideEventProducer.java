package com.upic.client;
import java.util.concurrent.BlockingQueue;

public class LiftRideEventProducer implements Runnable {
  private final BlockingQueue<String> eventQueue;
  private final int eventsToGenerate;

  public LiftRideEventProducer(BlockingQueue<String> eventQueue, int eventsToGenerate) {
    this.eventQueue = eventQueue;
    this.eventsToGenerate = eventsToGenerate;
  }
  @Override
  public void run() {
    System.out.println("LiftRideEventProducer started.");
    LiftRideEventGenerator generator = new LiftRideEventGenerator();
    for (int i = 0; i < eventsToGenerate; i++) {
      String event = generator.generateLiftRideEvent();
      try {
        eventQueue.put(event);
      } catch (InterruptedException e) {
        System.out.println("Producer was interrupted.");
        Thread.currentThread().interrupt();
        break;
      }
    }
    System.out.println("Producer finished generating " + eventsToGenerate + " events.");
  }
}


