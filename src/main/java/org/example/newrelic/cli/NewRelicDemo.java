package org.example.newrelic.cli;

import java.util.Random;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;

public class NewRelicDemo implements Runnable {

	public static void main(String[] args) throws Exception {
		System.out
				.println("Running demo in an infinite loop. Use Control-C to terminate program...");
		new NewRelicDemo().run();
		System.out.println("Finished");
	}

	public void run() {
		Random rand = new Random();
		long sleep = 0;
		int i = 0;
		while (true) {
			sleep = rand.nextInt(5000);
			try {
				doThing(i, sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i++;
		}
	}

	@Trace
	private void doThing(int idx, long sleep) throws InterruptedException {
		System.out.println(String.format("Going to sleep (%s)...", idx));
		Thread.sleep(sleep);
		// it is a good practice to namespace your metrics
		NewRelic.recordMetric("Custom/Demo/MyMetric", sleep);
		NewRelic.recordResponseTimeMetric("Custom/Demo/MyResponse", sleep);
	}
}
