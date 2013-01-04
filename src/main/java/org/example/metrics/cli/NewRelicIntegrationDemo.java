package org.example.metrics.cli;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.example.metrics.reporting.NewRelicReporter;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Meter;

public class NewRelicIntegrationDemo {

	private final Counter numRequests;
	private final Gauge<Long> freeMemoryGauge;
	private final Meter requests;

	public NewRelicIntegrationDemo(String licenseKey) {
		
		NewRelicReporter.enable(15, TimeUnit.SECONDS, licenseKey);
		
		numRequests = Metrics.newCounter(NewRelicIntegrationDemo.class,
				"call-counter");

		freeMemoryGauge = Metrics.newGauge(NewRelicIntegrationDemo.class,
				"jvm-free-mem", new Gauge<Long>() {

					@Override
					public Long value() {
						return Runtime.getRuntime().freeMemory();
					}
				});

		requests = Metrics.newMeter(NewRelicIntegrationDemo.class,
				"time-between-calls", "doThing", TimeUnit.MILLISECONDS);
	}

	public static void main(String[] args) {
		final String licenseKey = args[0];
		new NewRelicIntegrationDemo(licenseKey).run();
	}

	public void run() {
		System.out
				.println("This demo will run indefinately until it is terminated with Control-C");

		Random rand = new Random();
		long sleep;
		while (true) {
			try {
				sleep = rand.nextInt(5000);
				doThing(numRequests.count(), String.valueOf(sleep));
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void doThing(long index, String message) {
		numRequests.inc();
		requests.mark();
		System.out.println(String.format("Hello (%s)! Going to zzz for %s", index,
				message));
	}
}
