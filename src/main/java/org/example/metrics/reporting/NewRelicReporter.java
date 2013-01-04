package org.example.metrics.reporting;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import com.newrelic.api.agent.NewRelic;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.reporting.AbstractPollingReporter;

public class NewRelicReporter extends AbstractPollingReporter implements
		MetricProcessor<String> {

	public static final String NEWRELIC_METRIC_PREFIX = "Custom/Metrics/";

	protected NewRelicReporter(MetricsRegistry registry, String name) {
		super(registry, name);
	}

	public static void enable(long period, TimeUnit unit, String licenseKey) {
		final NewRelicReporter reporter = new NewRelicReporter(
				Metrics.defaultRegistry(), "newrelic-reporter");
		reporter.start(period, unit);
	}

	@Override
	public void run() {
		try {
			for (Entry<String, SortedMap<MetricName, Metric>> entry : getMetricsRegistry()
					.groupedMetrics(MetricPredicate.ALL).entrySet()) {
				for (Entry<MetricName, Metric> subEntry : entry.getValue()
						.entrySet()) {
					subEntry.getValue().processWith(this, subEntry.getKey(),
							null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processMeter(MetricName name, Metered meter, String context)
			throws Exception {

		final String METRIC_NAME_FORMAT = "%s.%s";
		sendToNewRelic(
				String.format(METRIC_NAME_FORMAT, name.getName(), "count"),
				meter.count());
		sendToNewRelic(String.format(METRIC_NAME_FORMAT, name.getName(),
				"1MinuteRate"), meter.oneMinuteRate());
		sendToNewRelic(String.format(METRIC_NAME_FORMAT, name.getName(),
				"15MinuteRate"), meter.fifteenMinuteRate());
		sendToNewRelic(String.format(METRIC_NAME_FORMAT, name.getName(),
				"5MinuteRate"), meter.fiveMinuteRate());
		sendToNewRelic(
				String.format(METRIC_NAME_FORMAT, name.getName(), "meanRate"),
				meter.meanRate());
	}

	@Override
	public void processCounter(MetricName name, Counter counter, String context)
			throws Exception {
		sendToNewRelic(name.getName(), Float.valueOf(counter.count()));
	}

	@Override
	public void processHistogram(MetricName name, Histogram histogram,
			String context) throws Exception {
		// TODO
		System.out.println("Unimplemented! " + name + ", " + histogram);
	}

	@Override
	public void processTimer(MetricName name, Timer timer, String context)
			throws Exception {
		// TODO there is more to export
		processMeter(name, timer, context);
	}

	@Override
	public void processGauge(MetricName name, Gauge<?> gauge, String context)
			throws Exception {
		String s = String.format("%s", gauge.value());
		Float f = new Float(s);
		sendToNewRelic(name.getName(), f);
	}

	private void sendToNewRelic(String name, double value) {
		sendToNewRelic(name, (float) value);
	}

	private void sendToNewRelic(String name, float value) {
		String key = NEWRELIC_METRIC_PREFIX + name;
		System.out.println("Recording metric to NewRelic: " + key + ": "
				+ value);
		NewRelic.recordMetric(key, value);
	}
}
