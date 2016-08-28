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
package com.polymathiccoder.servo.publish.influxdb.operations;

/*-
 * #%L
 * InfluxDBConnectionTest.java - servo-influxdb - PolymathicCoder LLC - 2,016
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



import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.unitils.UnitilsBlockJUnit4ClassRunner;

import com.polymathiccoder.servo.publish.influxdb.operations.InfluxDBConfiguration;
import com.polymathiccoder.servo.publish.influxdb.operations.InfluxDBConnection;
import com.polymathiccoder.servo.publish.influxdb.util.SampleData;

@PrepareForTest({ InfluxDBFactory.class })
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(UnitilsBlockJUnit4ClassRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.net.ssl.*"})
public class InfluxDBConnectionTest {

    // Mocks
    @Mock
    private InfluxDB influxDB;

    // Life Cycle
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mockStatic(InfluxDBFactory.class);
        when(InfluxDBFactory.connect(any(), any(), any())).thenReturn(influxDB);
    }

    // Tests: create
    @Test
    public void create() {
        // Parameters
        final InfluxDBConfiguration influxDBConfiguration = SampleData.getInfluxDBConfiguration();

        // Operation
        InfluxDBConnection.create(influxDBConfiguration);

        verifyStatic(times(1));
        InfluxDBFactory.connect(
                "http://localhost:8086",
                "root",
                "root");

        verify(influxDB, times(1)).enableBatch(1, 100, TimeUnit.MILLISECONDS);
    }
}
