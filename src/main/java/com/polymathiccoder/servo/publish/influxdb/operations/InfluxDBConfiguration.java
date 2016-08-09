/**
 * Copyright (c) 2016, Abdelmonaim Remani {@literal @}PolymathicCoder <PolymathicCoder@gmail.com>.
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

import java.util.concurrent.TimeUnit;

/**
 * An InfluxDB configuration.
 *
 * @author Abdelmonaim Remani, {@literal @}PolymathicCoder <PolymathicCoder@gmail.com>
 */
public class InfluxDBConfiguration {

    private final String url;

    private final String username;

    private final String password;

    private final String databaseName;

    private final BatchPolicy batchPolicy;

    /**
     * This constructor creates an InfluxDB configuration.
     *
     * @param url
     *            The URL of the InfluxDB instance.
     * @param username
     *            The username to access the InfluxDB instance.
     * @param password
     *            The password to access the InfluxDB instance.
     * @param databaseName
     *            The database name.
     * @param batchPolicy
     *            The policy to prescribe how data is to be written to InfluxDB
     *            in batches.
     */
    public InfluxDBConfiguration(final String url, final String username, final String password,
            final String databaseName, final BatchPolicy batchPolicy) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
        this.batchPolicy = batchPolicy;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getPassword() {
        return password;
    }

    public BatchPolicy getBatchPolicy() {
        return batchPolicy;
    }

    /**
     * The policy to prescribe how data is to be written to InfluxDB in batches.
     *
     * @author vvs148
     */
    public static class BatchPolicy {
        private final int flushEveryPoints;

        private final int flushAtLeastEvery;

        private final TimeUnit flushAtLeastEveryTimeUnit;

        /**
         * This constructor creates the policy prescribing how data is to be
         * written to InfluxDB in batches.
         *
         * @param flushEveryPoints
         *            The maximum number of data points before writing the data
         *            to the InfluxDB instance.
         * @param flushAtLeastEvery
         *            The longest duration to wait before writing the data to
         *            the InfluxDB instance.
         * @param flushAtLeastEveryTimeUnit
         *            The time unit of longest duration to wait before writing
         *            the data to the InfluxDB instance.
         */
        public BatchPolicy(final int flushEveryPoints, final int flushAtLeastEvery,
                final TimeUnit flushAtLeastEveryTimeUnit) {
            this.flushEveryPoints = flushEveryPoints;
            this.flushAtLeastEvery = flushAtLeastEvery;
            this.flushAtLeastEveryTimeUnit = flushAtLeastEveryTimeUnit;
        }

        public int getFlushEveryPoints() {
            return flushEveryPoints;
        }

        public int getFlushAtLeastEvery() {
            return flushAtLeastEvery;
        }

        public TimeUnit getFlushAtLeastEveryTimeUnit() {
            return flushAtLeastEveryTimeUnit;
        }
    }
}
