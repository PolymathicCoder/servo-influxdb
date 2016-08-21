/**
 * Copyright (c) 2016, Abdelmonaim Remani {@literal @}PolymathicCoder PolymathicCoder@gmail.com.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.polymathiccoder.servo.publish.influxdb;

/*-
 * #%L
 * InfluxDBMetricObserver.java - servo-influxdb - PolymathicCoder LLC - 2,016
 * %%
 * Copyright (C) 2016 PolymathicCoder LLC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static com.netflix.servo.annotations.DataSourceType.COUNTER;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.netflix.servo.Metric;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.annotations.MonitorTags;
import com.netflix.servo.monitor.Monitors;
import com.netflix.servo.monitor.Stopwatch;
import com.netflix.servo.monitor.Timer;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.tag.SortedTagList;
import com.netflix.servo.tag.Tag;
import com.netflix.servo.tag.TagList;
import com.polymathiccoder.servo.publish.influxdb.operations.InfluxDBOperations;
import com.polymathiccoder.servo.publish.influxdb.operations.error.InfluxDBClientException;
import com.polymathiccoder.servo.publish.influxdb.operations.error.InfluxDBConnectionException;
import com.polymathiccoder.servo.publish.influxdb.operations.error.InfluxDBOperationException;

/**
 * An Observer that forwards metrics to an InfluxDB instance.
 *
 * @author Abdelmonaim Remani, {@literal @}PolymathicCoder
 *         PolymathicCoder@gmail.com
 */
public class InfluxDBMetricObserver implements MetricObserver {

    protected static final Logger LOGGER = LoggerFactory.getLogger(InfluxDBMetricObserver.class);

    private final InfluxDBOperations influxDBOperations;

    private final List<MetricTransformation> metricTransformations;

    private final InfluxDBMetricObserverMetrics influxDBMetricObserverMetrics;

    /**
     * This constructor creates an observer that forwards data in batches to an
     * InfluxDB instance according to a configuration. It also allows for
     * applying transformations to data point before it writing them to the
     * database, such as appending additional tags. If no database exists with
     * the name said in the configuration, it will be automatically created.
     *
     * @param influxDBOperations
     *            XXXX.
     * @param metricTransformations
     *            A list of closures allowing for transforming metrics before
     *            writing them to the database. The closure are applied in the
     *            same order they were passed as parameters.
     */
    public InfluxDBMetricObserver(final InfluxDBOperations influxDBOperations,
            final MetricTransformation... metricTransformations) {
        this.influxDBOperations = influxDBOperations;
        this.metricTransformations = metricTransformations.length != 0
                ? Lists.newArrayList(metricTransformations) : Lists.newArrayList();

        // Create the database
        try {
            influxDBOperations.createDatabase();
        } catch (final InfluxDBClientException influxDBClientException) {
            LOGGER.error(influxDBClientException.getMessage());
        }

        // Register Metrics
        influxDBMetricObserverMetrics = new InfluxDBMetricObserverMetrics(new HashSet<>());
    }

    @Override
    public void update(final List<Metric> metrics) {
        final Stopwatch stopwatch = influxDBMetricObserverMetrics.influxDBReportingTimer.start();
        influxDBMetricObserverMetrics.numMetricsTotal.addAndGet(metrics.size());

        // @formatter:off
        List<Point> points = metrics.stream()
                .map((metric) -> metricTransformations.stream().reduce(metric,
                        (m, mt) -> mt.apply(m), (m1, m2) -> m1))
                .map((metric) -> Point.measurement(metric.getConfig().getName())
                        .time(metric.getTimestamp(), TimeUnit.MILLISECONDS)
                        .tag(metric.getConfig().getTags().asMap())
                        .addField("value", metric.getNumberValue()).build())
                .collect(toList());
        // @formatter:on

        try {
            LOGGER.debug("Writing {} metrics to the InfluxDB instance", metrics.size());
            influxDBOperations.writeDataInBatch(points);
            influxDBMetricObserverMetrics.numMetricsSent.addAndGet(metrics.size());
        } catch (final InfluxDBConnectionException.RefusedException
                | InfluxDBConnectionException.TimedOutException influxDBClientException) {
            influxDBMetricObserverMetrics.numMetricsDroppedSendTimeout.incrementAndGet();
        } catch (final InfluxDBOperationException.DataManipulationException.DataWriteOperationException influxDBClientException) {
            influxDBMetricObserverMetrics.numMetricsDroppedHttpErr.incrementAndGet();
        } finally {
            influxDBMetricObserverMetrics.numMetricsTotal.addAndGet(metrics.size());
        }

        stopwatch.stop();
    }

    @Override
    public String getName() {
        return "influxDBMetricObserver";
    }

    // Types
    public static class InfluxDBMetricObserverMetrics {
        private final Timer influxDBReportingTimer = Monitors.newTimer("influxDBReportingTime");

        @Monitor(name = "numMetricsTotal", type = COUNTER)
        private final AtomicInteger numMetricsTotal = new AtomicInteger(0);

        @Monitor(name = "numMetricsSent", type = COUNTER)
        private final AtomicInteger numMetricsSent = new AtomicInteger(0);

        @Monitor(name = "numMetricsDroppedSendTimeout", type = DataSourceType.COUNTER)
        private final AtomicInteger numMetricsDroppedSendTimeout = new AtomicInteger(0);

        @Monitor(name = "numMetricsDroppedHttpErr", type = COUNTER)
        private final AtomicInteger numMetricsDroppedHttpErr = new AtomicInteger(0);

        @MonitorTags
        private final TagList tags;

        public InfluxDBMetricObserverMetrics(final Collection<Tag> tags) {
            this.tags = SortedTagList.builder().withTags(tags).build();
        }

        {
            Monitors.registerObject("influxDBMetricObserver", this);
        }
    }
}
