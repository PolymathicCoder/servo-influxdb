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
package com.polymathiccoder.servo.publish.influxdb.util;

/*-
 * #%L
 * SampleData.java - servo-influxdb - PolymathicCoder LLC - 2,016
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



import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.netflix.servo.Metric;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.tag.BasicTag;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.tag.Tag;
import com.polymathiccoder.servo.publish.influxdb.MetricTransformation;
import com.polymathiccoder.servo.publish.influxdb.operations.InfluxDBConfiguration;

public final class SampleData {
    public static List<Metric> getMetrics(final int discriminator, final long timestampInMillis) {
        final MonitorConfig monitorConfig = MonitorConfig
                .builder("metric-" + discriminator)
                .withTags(
                        BasicTagList.of(
                                new BasicTag("tag1", "t1"),
                                new BasicTag("tag2", "t2"),
                                new BasicTag("tag3", "t3")))
                .build();

        return Lists.newArrayList(
                new Metric(
                        monitorConfig,
                        timestampInMillis,
                        1),
                new Metric(
                        monitorConfig,
                        timestampInMillis,
                        2),
                new Metric(
                        monitorConfig,
                        timestampInMillis,
                        3));
    }

    public static List<Point> getValidPointList(final int discriminator, final long timestampInMillis) {
        final String measurementName = "metric-" + discriminator;
        final Map<String, String> tags = ImmutableMap.<String, String>builder()
            .put("tag1", "t1")
            .put("tag2", "t2")
            .put("tag3", "t3")
            .build();

        return Lists.newArrayList(
                Point.measurement(measurementName)
                    .time(timestampInMillis, TimeUnit.MILLISECONDS)
                    .addField("value", 1)
                    .tag(tags)
                    .build(),
                Point.measurement(measurementName)
                    .time(timestampInMillis, TimeUnit.MILLISECONDS)
                    .addField("value", 2)
                    .tag(tags)
                    .build(),
                Point.measurement(measurementName)
                    .time(timestampInMillis, TimeUnit.MILLISECONDS)
                    .addField("value", 3)
                    .tag(tags)
                    .build());
    }

    public static List<Point> getValidPointListWithExtraTags(final int discriminator, final long timestampInMillis) {
        final String measurementName = "metric-" + discriminator;
        final Map<String, String> tags = ImmutableMap.<String, String>builder()
            .put("tag1", "t1")
            .put("tag2", "t2")
            .put("tag3", "t3")
            .put("extraTag1", "xt1")
            .put("extraTag2", "xt2")
            .build();

        return Lists.newArrayList(
                Point.measurement(measurementName)
                    .time(timestampInMillis, TimeUnit.MILLISECONDS)
                    .addField("value", 1)
                    .tag(tags)
                    .build(),
                Point.measurement(measurementName)
                    .time(timestampInMillis, TimeUnit.MILLISECONDS)
                    .addField("value", 2)
                    .tag(tags)
                    .build(),
                Point.measurement(measurementName)
                    .time(timestampInMillis, TimeUnit.MILLISECONDS)
                    .addField("value", 3)
                    .tag(tags)
                    .build());
    }

    public static List<MetricTransformation> getSampleMetricTransformations() {
        return Lists.newArrayList(
                (metric) -> new Metric(
                    metric.getConfig().getName(),
                    BasicTagList.concat(metric.getConfig().getTags(), new Tag[] { new BasicTag("extraTag1", "xt1") }),
                    metric.getTimestamp(),
                    metric.getNumberValue()),
                (metric) -> new Metric(
                    metric.getConfig().getName(),
                    BasicTagList.concat(metric.getConfig().getTags(), new Tag[] { new BasicTag("extraTag2", "xt2") }),
                    metric.getTimestamp(),
                    metric.getNumberValue()));
    }

    public static InfluxDBConfiguration getInfluxDBConfiguration() {
        return new InfluxDBConfiguration(
                "http://localhost:8086",
                "root",
                "root",
                "metrics",
                new InfluxDBConfiguration.BatchPolicy(
                        1,
                        100,
                        TimeUnit.MILLISECONDS));
    }
}
