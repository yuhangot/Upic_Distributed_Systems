package com.upic.client;
import java.util.Random;

public class LiftRideEventGenerator {
  private final Random rand = new Random();

  public String generateLiftRideEvent() {
    int skierId = rand.nextInt(100000) + 1;
    int resortId = rand.nextInt(10) + 1;
    int liftId = rand.nextInt(40) + 1;
    int seasonId = 2024;
    int dayId = 1;
    int time = rand.nextInt(360) + 1;

    return String.format(
        "{\"skierId\": \"%d\", \"resortId\": \"%d\", \"liftId\": \"%d\", \"seasonId\": \"%d\", \"dayId\": \"%d\", \"time\": \"%d\"}",
        skierId, resortId, liftId, seasonId, dayId, time
    );
  }
}
