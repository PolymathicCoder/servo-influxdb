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

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.polymathiccoder.servo.publish.influxdb.operations.error.InfluxDBClientException;
import com.polymathiccoder.servo.publish.influxdb.operations.error.InfluxDBConnectionException;
import com.polymathiccoder.servo.publish.influxdb.operations.error.InfluxDBOperationException;

import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;

/**
 * An Observer that forwards metrics to an InfluxDB instance.
 *
 * @author Abdelmonaim Remani, {@literal @}PolymathicCoder
 *         PolymathicCoder@gmail.com
 */
public class InfluxDBOperations {

    private final InfluxDBConnection influxDBConnection;

    public InfluxDBOperations(final InfluxDBConnection timeseriesDatagateway) {
        this.influxDBConnection = timeseriesDatagateway;
    }

    public void createDatabase() throws InfluxDBClientException {
        final InfluxDBConfiguration configuration = influxDBConnection.getConfiguration();
        try {
            influxDBConnection.getDBInstance().createDatabase(configuration.getDatabaseName());
        } catch (final RetrofitError retrofitError) {
            handleException(
                    retrofitError,
                    new InfluxDBOperationException.DataDefinitionException.DatabaseCreationException(
                            influxDBConnection.getConfiguration()));
        }
    }

    public void writeDataInBatch(final List<Point> points) throws InfluxDBClientException {
        final BatchPoints batchPoints = BatchPoints
                .database(influxDBConnection.getConfiguration().getDatabaseName())
                .retentionPolicy("default").consistency(ConsistencyLevel.ALL).build();

        points.forEach((point) -> batchPoints.point(point));

        try {
            influxDBConnection.getDBInstance().write(batchPoints);
        } catch (final RetrofitError exception) {
            handleException(
                    exception,
                    new InfluxDBOperationException.DataManipulationException.DataWriteOperationException(
                            influxDBConnection.getConfiguration(),
                            points));
        }
    }

    private void handleException(final RetrofitError thrown, final InfluxDBClientException toThrow) {
        switch (thrown.getKind()) {
        case NETWORK:
            if (thrown.getCause() instanceof SocketException) {
                throw new InfluxDBConnectionException.RefusedException(
                        influxDBConnection.getConfiguration());
            } else if (thrown.getCause() instanceof SocketTimeoutException) {
                throw new InfluxDBConnectionException.TimedOutException(
                        influxDBConnection.getConfiguration());
            } else {
                throw new InfluxDBConnectionException(
                        influxDBConnection.getConfiguration());
            }
        case HTTP:
        case CONVERSION:
        default:
            String reason = "";
            if (thrown.getResponse().getBody() != null) {
                final String responseBody = new String(((TypedByteArray) thrown.getResponse().getBody()).getBytes());
                try {
                    final JsonObject json = new JsonParser().parse(responseBody).getAsJsonObject();
                    if (json.has("error")) {
                        reason = json.get("error").getAsString();
                    } else {
                        reason = json.toString();
                    }
                } catch (final IllegalStateException | JsonSyntaxException exception) {
                    reason = responseBody;
                }
            } else {
                reason = "";
            }
            reason = reason.isEmpty() ? reason : " - " + reason;
            toThrow.setReason("The service returned '" + thrown.getResponse().getStatus() + " " + thrown.getResponse().getReason() + reason + "'");
            throw toThrow;
        }
    }
}
