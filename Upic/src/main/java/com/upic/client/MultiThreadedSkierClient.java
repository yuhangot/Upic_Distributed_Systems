package com.upic.client;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadedSkierClient {
  private static final int NUMBER_OF_THREADS = 32;
  private static final int TOTAL_EVENTS = 200000;
  private static final int EVENTS_PER_THREAD = 1000;
  private static final int REMAIN_NUMBER_OF_THREADS = 360;
  private static final String BASE_URL = "http://35.165.54.14:8080/Upic_war/";
  private static final String ENDPOINT = "skiers";
  private static final int MAX_RETRIES = 5;
  private static final AtomicInteger successfulRequests = new AtomicInteger(0);
  private static final AtomicInteger failedRequests = new AtomicInteger(0);
  private static final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
  // Add a static variable to store single request latency
  private static long singleRequestLatency = -1;

  public static void main(String[] args) {
    SkierClient skierClient = new SkierClient(BASE_URL);

    // Test single request latency
    testSingleRequestLatency(skierClient, ENDPOINT);

    //The first thread pool to process the first 32000 and terminate
    ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    BlockingQueue<String> eventQueue = new LinkedBlockingQueue<>();
    new Thread(new LiftRideEventProducer(eventQueue, TOTAL_EVENTS)).start();
    long startTime = System.currentTimeMillis();

    for (int i = 0; i < NUMBER_OF_THREADS; i++) {
      executorService.execute(() -> {
        int eventsSent = 0;
        while (eventsSent < EVENTS_PER_THREAD) {
          String event;
          try {
            event = eventQueue.poll(10, TimeUnit.SECONDS);
            if (event == null) {
              break;
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return; 
          }

          long sendTime = System.currentTimeMillis();
          int responseCode = sendRequestWithRetries(skierClient, ENDPOINT, event);
          long endTime = System.currentTimeMillis();
          long latency = endTime - sendTime;

          writeRecordToCSV(sendTime, "POST", latency, responseCode);
          latencies.add(latency);

          if (responseCode == 201) {
            successfulRequests.incrementAndGet();
          } else {
            failedRequests.incrementAndGet();
          }

          eventsSent++;
        }
      });
    }
    executorService.shutdown();
    try {
      executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("Execution completed. Successful requests: " + successfulRequests.get() + ", Failed requests: " + failedRequests.get());

//The second thread pool to process the rest of event
    ExecutorService executorService2 = Executors.newFixedThreadPool(REMAIN_NUMBER_OF_THREADS);
    int remainingEvents = TOTAL_EVENTS - successfulRequests.get();
    for (int i = 0; i < remainingEvents; i++) {
      executorService2.execute(() -> {
        String event;
        try {
          event = eventQueue.poll(10, TimeUnit.SECONDS);
          if (event != null) {
            long sendTime = System.currentTimeMillis();
            int responseCode = sendRequestWithRetries(skierClient, ENDPOINT, event);
            long endTime = System.currentTimeMillis();
            long latency = endTime - sendTime;

            writeRecordToCSV(sendTime, "POST", latency, responseCode);
            latencies.add(latency);

            if (responseCode == 201) {
              successfulRequests.incrementAndGet();
            } else {
              failedRequests.incrementAndGet();
            }
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
    }
    executorService2.shutdown();
    try {
      executorService2.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("Execution completed. Successful requests: " + successfulRequests.get() + ", Failed requests: " + failedRequests.get());

    long totalTime = System.currentTimeMillis() - startTime;
    printStats(totalTime);
  }

  // Test the single request latency to compare
  private static void testSingleRequestLatency(SkierClient client, String endpoint) {

    LiftRideEventGenerator generator = new LiftRideEventGenerator();
    String sampleEvent = generator.generateLiftRideEvent();

    try {
      long startTime = System.currentTimeMillis();
      int responseCode = client.sendPostRequest(endpoint, sampleEvent);
      long endTime = System.currentTimeMillis();
      singleRequestLatency = endTime - startTime; // Update the static variable

      // Optionally, you can still print it here or just leave the printing to printStats
      System.out.println("Single request latency test complete. Result will be printed in final stats.");
    } catch (IOException | InterruptedException e) {
      System.err.println("Error during single request latency test: " + e.getMessage());
    }
  }

  static int sendRequestWithRetries(SkierClient client, String endpoint, String body) {
    int responseCode = 0;
    int retries = 0;
    while (retries < MAX_RETRIES) {
      try {
        responseCode = client.sendPostRequest(endpoint, body);
        if (responseCode == 201) {
          System.out.println("Request successful with status code: " + responseCode);
          return responseCode;
        } else {
          System.out.println("Request completed with non-success status code: " + responseCode);
          retries++;
        }
      } catch (IOException | InterruptedException e) {
        System.out.println("Attempt " + (retries + 1) + " failed with message: " + e.getMessage());
        retries++;
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          return responseCode;
        }
      }
    }
    return responseCode;
  }

  private static synchronized void writeRecordToCSV(long startTime, String requestType, long latency, int responseCode) {
    try (FileWriter fw = new FileWriter("latencies.csv", true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw)) {
      out.println(startTime + "," + requestType + "," + latency + "," + responseCode);
    } catch (IOException e) {
      System.err.println("Error writing to CSV file: " + e.getMessage());
    }
  }

  /***
   * This is Part2 of Client that print out the static information
   * **/
  private static void printStats(long totalTime) {
    System.out.println("Total successful requests: " + successfulRequests.get());
    System.out.println("Total failed requests: " + failedRequests.get());
    System.out.println("Total run time: " + totalTime + " ms");

    // Check if the single request latency has be recorded or not
    if (singleRequestLatency != -1) {
      System.out.println("Single request latency: " + singleRequestLatency + " ms");
    } else {
      System.out.println("Single request latency not recorded.");
    }

    double throughput = successfulRequests.get() / (totalTime / 1000.0);
    System.out.println("Throughput: " + throughput + " requests/second");

    Collections.sort(latencies);
    long sum = 0;
    for (long latency : latencies) {
      sum += latency;
    }
    double mean = (double) sum / latencies.size();
    System.out.println("Mean response time: " + mean + " ms");
    System.out.println("Median response time: " + latencies.get(latencies.size() / 2) + " ms");
    System.out.println("P99 response time: " + latencies.get((int) (latencies.size() * 0.99)) + " ms");
    System.out.println("Min response time: " + latencies.get(0) + " ms");
    System.out.println("Max response time: " + latencies.get(latencies.size() - 1) + " ms");
  }
}
