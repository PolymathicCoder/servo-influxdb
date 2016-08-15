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

import java.lang.ref.WeakReference;
import java.util.List;

import org.influxdb.dto.Point;

import com.polymathiccoder.servo.publish.influxdb.operations.InfluxDBConfiguration;

@SuppressWarnings("serial")
public class InfluxDBOperationException extends InfluxDBClientException {

    private static String MESSAGE_TEMPLATE = "The operation failed to successfully execute%s.";

    public InfluxDBOperationException(final InfluxDBConfiguration influxDBConfiguration) {
        super(influxDBConfiguration);
    }

    @Override
    public String getMessage() {
        return String.format(MESSAGE_TEMPLATE, reason.isEmpty() ? reason : " because: " + reason);
    }

    public static class DataManipulationException extends InfluxDBOperationException {

        public DataManipulationException(final InfluxDBConfiguration influxDBConfiguration) {
            super(influxDBConfiguration);
        }

        public static class DataWriteOperationException extends DataManipulationException {

            private static String MESSAGE_TEMPLATE = "Could not write %d data points.";

            private final WeakReference<List<Point>> points;

            public DataWriteOperationException(final InfluxDBConfiguration influxDBConfiguration, final List<Point> points) {
                super(influxDBConfiguration);
                this.points = new WeakReference<List<Point>>(points);
                LOGGER.error(getMessage());
            }

            @Override
            public String getMessage() {
                return String.format(super.getMessage() + " " + MESSAGE_TEMPLATE, points.get().size());
            }
        }
    }

    public static class DataDefinitionException extends InfluxDBOperationException {

        public DataDefinitionException(final InfluxDBConfiguration influxDBConfiguration) {
            super(influxDBConfiguration);
        }

        public static class DatabaseCreationException extends DataDefinitionException {

            private static String MESSAGE_TEMPLATE = "Could not create a database with the name '%s'.";

            public DatabaseCreationException(final InfluxDBConfiguration influxDBConfiguration) {
                super(influxDBConfiguration);
                LOGGER.error(getMessage());
            }

            @Override
            public String getMessage() {
                return String.format(super.getMessage() + " " + MESSAGE_TEMPLATE, influxDBConfiguration.get().getDatabaseName());
            }
        }
    }
}