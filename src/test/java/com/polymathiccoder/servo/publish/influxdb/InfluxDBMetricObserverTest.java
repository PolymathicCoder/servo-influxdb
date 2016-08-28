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
 * InfluxDBMetricObserverTest.java - servo-influxdb - PolymathicCoder LLC - 2,016
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



import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.influxdb.dto.Point;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import com.netflix.servo.Metric;
import com.polymathiccoder.servo.publish.influxdb.InfluxDBMetricObserver;
import com.polymathiccoder.servo.publish.influxdb.MetricTransformation;
import com.polymathiccoder.servo.publish.influxdb.operations.InfluxDBOperations;
import com.polymathiccoder.servo.publish.influxdb.util.SampleData;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.net.ssl.*"})
public class InfluxDBMetricObserverTest {

    //@InjectMocks
    private InfluxDBMetricObserver influxDBMetricObserver;

    // Mocks & Spies
    @Mock
    private InfluxDBOperations influxDBOperations;

    @Spy
    private ArrayList<MetricTransformation> metricTransformations;

    // Parameters & Captors
    @Captor
    private ArgumentCaptor<List<Point>> points;

    // Others
    private int discriminator;
    private long timestampInMillis;

    // Life Cycle
    @Before
    public void setup() {
        discriminator = new Random().nextInt(1000);
        timestampInMillis = System.currentTimeMillis();
    }

    // Tests: writeDataInBatch
    @Test
    public void update_success_validParams_noMetricTransformation() {
        // Parameters
        final List<Metric> metrics = SampleData.getMetrics(discriminator, timestampInMillis);

        // Object under Test Initialization
        influxDBMetricObserver = new InfluxDBMetricObserver(influxDBOperations);

        // Operation
        influxDBMetricObserver.update(metrics);

        // Verifications and Assertions
        verify(influxDBOperations, times(1)).writeDataInBatch(points.capture());

        final List<Point> expectedPoints = SampleData.getValidPointList(discriminator, timestampInMillis);
        assertThat(
                points.getValue().stream().map(Point::lineProtocol).collect(toList()))
        .containsExactlyElementsOf(
                expectedPoints.stream().map(Point::lineProtocol).collect(toList()));
    }

    @Test
    public void update_success_validParams_withMetricTransformation() {
        // Parameters
        final List<Metric> metrics = SampleData.getMetrics(discriminator, timestampInMillis);
        final MetricTransformation[] metricTransformations = SampleData.getSampleMetricTransformations().toArray(new MetricTransformation[SampleData.getSampleMetricTransformations().size()]);

        // Object under Test Initialization
        influxDBMetricObserver = new InfluxDBMetricObserver(
                influxDBOperations,
                metricTransformations);

        // Operation
        influxDBMetricObserver.update(metrics);

        // Verifications and Assertions
        verify(influxDBOperations, times(1)).writeDataInBatch(points.capture());

        final List<Point> expectedPoints = SampleData.getValidPointListWithExtraTags(discriminator, timestampInMillis);
        assertThat(
                points.getValue().stream().map(Point::lineProtocol).collect(toList()))
        .containsExactlyElementsOf(
                expectedPoints.stream().map(Point::lineProtocol).collect(toList()));
    }
}
