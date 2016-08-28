/*-
 * #%L
 * Main.java - servo-influxdb - PolymathicCoder LLC - 2,016
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

import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.JvmMetricPoller;
import com.netflix.servo.publish.MetricPoller;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;
import com.polymathiccoder.servo.publish.influxdb.InfluxDBMetricObserver;
import com.polymathiccoder.servo.publish.influxdb.operations.InfluxDBConnection;
import com.polymathiccoder.servo.publish.influxdb.operations.InfluxDBOperations;
import com.polymathiccoder.servo.publish.influxdb.util.NetflixServoUtils;
import com.polymathiccoder.servo.publish.influxdb.util.SampleData;

public class Main {
    public static void main(String[] args) {
        // Observers
        final InfluxDBConnection influxDBConnection = InfluxDBConnection.create(SampleData.getInfluxDBConfiguration());
        final InfluxDBOperations influxDBOperations = new InfluxDBOperations(influxDBConnection);
        final InfluxDBMetricObserver influxDBMetricObserver = new InfluxDBMetricObserver(
                influxDBOperations,
                NetflixServoUtils.commonTagsDecoration());

        // Pollers
        MetricPoller monitorRegistryMetricPoller = new MonitorRegistryMetricPoller();
        MetricPoller JvmMetricPoller = new JvmMetricPoller();

        // Schedulers
        final PollScheduler pollScheduler = PollScheduler.getInstance();
        pollScheduler.start();

        final PollRunnable jvmMetricsTask = new PollRunnable(
                JvmMetricPoller,
                BasicMetricFilter.MATCH_ALL,
                true,
                Lists.newArrayList(
                        influxDBMetricObserver));
        pollScheduler.addPoller(jvmMetricsTask, 3, TimeUnit.SECONDS);

        final PollRunnable monitorRegistryMetricsTask = new PollRunnable(
                monitorRegistryMetricPoller,
                BasicMetricFilter.MATCH_ALL,
                true,
                Lists.newArrayList(
                        influxDBMetricObserver));
        pollScheduler.addPoller(monitorRegistryMetricsTask, 3, TimeUnit.SECONDS);
    }
}
