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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.netflix.servo.Metric;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.tag.BasicTag;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.tag.Tag;
import com.polymathiccoder.servo.publish.influxdb.MetricTransformation;

public final class NetflixServoUtils {

    private static final Tag ATLAS_COUNTER_TAG = new BasicTag("atlas.dstype", "counter");
    private static final Tag ATLAS_GAUGE_TAG = new BasicTag("atlas.dstype", "gauge");

    private static final String CLUSTER = "nf.cluster";
    private static final String NODE = "nf.node";
    private static final String UNKNOWN = "unknown";

    public static MetricTransformation commonTagsDecoration(){
        return (metric) -> {
            final Set<Tag> tags = Sets.newHashSet(Iterables.transform(metric.getConfig().getTags(),
                    (it) -> new BasicTag(it.getKey(), it.getValue())));

            // Add legacy Atlas tag
            if (isCounter(metric)) {
                tags.add(ATLAS_COUNTER_TAG);
            } else if (isGauge(metric)) {
                tags.add(ATLAS_GAUGE_TAG);
            } else if (isRate(metric)) {
                tags.add(ATLAS_COUNTER_TAG);
            }

            // Add cluster tag
            final String cluster = System.getenv("NETFLIX_CLUSTER");
            tags.add(new BasicTag(CLUSTER, (cluster == null) ? UNKNOWN : cluster));

            // Add node tag
            try {
                tags.add(new BasicTag(NODE, InetAddress.getLocalHost().getHostName()));
            } catch (final UnknownHostException unknownHostException) {
                tags.add(new BasicTag(NODE, UNKNOWN));
            }

            return new Metric(metric.getConfig().getName(),
                    BasicTagList.of(tags.toArray(new Tag[tags.size()])), metric.getTimestamp(),
                    metric.getNumberValue());
        };
    }

    private static boolean isCounter(final Metric metric) {
        final String value = metric.getConfig().getTags().getValue(DataSourceType.KEY);
        return value != null && value.equals(DataSourceType.COUNTER.name());
    }

    private static boolean isGauge(final Metric metric) {
        final String value = metric.getConfig().getTags().getValue(DataSourceType.KEY);
        return value != null && value.equals(DataSourceType.GAUGE.name());
    }

    private static boolean isRate(final Metric metric) {
        final String value = metric.getConfig().getTags().getValue(DataSourceType.KEY);
        return DataSourceType.RATE.name().equals(value)
                || DataSourceType.NORMALIZED.name().equals(value);
    }
}
