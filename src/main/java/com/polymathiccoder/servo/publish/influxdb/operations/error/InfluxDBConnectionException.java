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
package com.polymathiccoder.servo.publish.influxdb.operations.error;

import com.polymathiccoder.servo.publish.influxdb.operations.InfluxDBConfiguration;

@SuppressWarnings("serial")
public class InfluxDBConnectionException extends InfluxDBClientException {

    private static String MESSAGE_TEMPLATE = "The InfluxDB instance at %s is unreachable.";

    public InfluxDBConnectionException(final InfluxDBConfiguration influxDBConfiguration) {
        super(influxDBConfiguration);
    }

    @Override
    public String getMessage() {
        return String.format(MESSAGE_TEMPLATE, influxDBConfiguration.get().getUrl());
    }

    public static class TimedOutException extends InfluxDBConnectionException {

        private static String MESSAGE_TEMPLATE = "The connection timed out.";

        public TimedOutException(final InfluxDBConfiguration influxDBConfiguration) {
            super(influxDBConfiguration);
            LOGGER.error(getMessage());
        }

        @Override
        public String getMessage() {
            return String.format(super.getMessage() + " " + MESSAGE_TEMPLATE);
        }
    }

    public static class RefusedException extends InfluxDBConnectionException {

        private static String MESSAGE_TEMPLATE = "The connection was refused.";

        public RefusedException(final InfluxDBConfiguration influxDBConfiguration) {
            super(influxDBConfiguration);
            LOGGER.error(getMessage());
        }

        @Override
        public String getMessage() {
            return String.format(super.getMessage() + " " + MESSAGE_TEMPLATE);
        }
    }
}