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

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

public class InfluxDBConnection {

    private final InfluxDBConfiguration configuration;

    private final InfluxDB dbInstance;

    private InfluxDBConnection(final InfluxDBConfiguration configuration) {
        this.configuration = configuration;

        // Initialize the InfluxDB client
        dbInstance = InfluxDBFactory.connect(configuration.getUrl(),
                configuration.getUsername(), configuration.getPassword());

        // Enable batching
        dbInstance.enableBatch(configuration.getBatchPolicy().getFlushEveryPoints(),
                configuration.getBatchPolicy().getFlushAtLeastEvery(),
                configuration.getBatchPolicy().getFlushAtLeastEveryTimeUnit());
    }

    public static InfluxDBConnection create(final InfluxDBConfiguration configuration) {
        return new InfluxDBConnection(configuration);
    }

    public InfluxDBConfiguration getConfiguration() {
        return configuration;
    }

    public InfluxDB getDBInstance() {
        return dbInstance;
    }
}
