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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Random;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polymathiccoder.servo.publish.influxdb.operations.error.InfluxDBClientException;
import com.polymathiccoder.servo.publish.influxdb.operations.error.InfluxDBConnectionException;
import com.polymathiccoder.servo.publish.influxdb.operations.error.InfluxDBOperationException;
import com.polymathiccoder.servo.publish.influxdb.util.SampleAPIResponses;
import com.polymathiccoder.servo.publish.influxdb.util.SampleData;

import retrofit.RetrofitError;

@PrepareForTest({ LoggerFactory.class })
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
public class InfluxDBOperationsTest {

    @InjectMocks
    private InfluxDBOperations influxDBOperations;

    // Mocks
    @Mock
    private InfluxDBConnection influxDBConnection;

    @Mock
    private InfluxDB influxDB;

    @Mock
    protected Logger logger;

    // Other
    private int discriminator;
    private long timestampInMillis;

    // Life Cycle
    @Before
    public void setup() {
        discriminator = new Random().nextInt(1000);
        timestampInMillis = System.currentTimeMillis();

        mockStatic(LoggerFactory.class);
        when(LoggerFactory.getLogger(InfluxDBClientException.class)).thenReturn(logger);
        doNothing().when(logger).debug(any());
        doNothing().when(logger).error(any());
        doNothing().when(logger).info(any());

        doReturn(SampleData.getInfluxDBConfiguration()).when(influxDBConnection).getConfiguration();
        doReturn(influxDB).when(influxDBConnection).getDBInstance();
    }

    // Tests: writeDataInBatch
    @Test
    public void writeDataInBatch_success_validParams() throws InfluxDBClientException {
        final List<Point> points = SampleData.getValidPointList(discriminator, timestampInMillis);

        influxDBOperations.writeDataInBatch(points);

        verify(influxDB, times(1)).write(any());
    }

    @Test
    public void writeDataInBatch_failure_seriveProblem_wellFormedValidApiError() throws InfluxDBClientException {
        final List<Point> points = SampleData.getValidPointList(discriminator, timestampInMillis);

        doThrow(SampleAPIResponses.getHTTPErrorWithWellFormedValidResponseBody("Bad data"))
            .when(influxDB).write(any());

        assertThatThrownBy(() -> influxDBOperations.writeDataInBatch(points))
            .isInstanceOf(InfluxDBOperationException.DataManipulationException.DataWriteOperationException.class)
            .hasMessage("The operation failed to successfully execute because: The service returned '500 Internal Error - Bad data'. Could not write %d data points.", points.size());
    }

    @Test
    public void writeDataInBatch_failure_seriveProblem_wellFormedInvalidApiError() throws InfluxDBClientException {
        final List<Point> points = SampleData.getValidPointList(discriminator, timestampInMillis);

        doThrow(SampleAPIResponses.getHTTPErrorWithWellFormedInvalidResponseBody("Bad data"))
            .when(influxDB).write(any());

        assertThatThrownBy(() -> influxDBOperations.writeDataInBatch(points))
            .isInstanceOf(InfluxDBOperationException.DataManipulationException.DataWriteOperationException.class)
            .hasMessage("The operation failed to successfully execute because: The service returned '500 Internal Error - {\"x\":\"Bad data\"}'. Could not write 3 data points.");
    }

    @Test
    public void writeDataInBatch_failure_seriveProblem_badlyFormedApiError() throws InfluxDBClientException {
        final List<Point> points = SampleData.getValidPointList(discriminator, timestampInMillis);

        doThrow(SampleAPIResponses.getHTTPErrorWithBadlyFormedResponseBody("Bad data"))
            .when(influxDB).write(any());

        assertThatThrownBy(() -> influxDBOperations.writeDataInBatch(points))
            .isInstanceOf(InfluxDBOperationException.DataManipulationException.DataWriteOperationException.class)
            .hasMessage("The operation failed to successfully execute because: The service returned '500 Internal Error - Bad data'. Could not write 3 data points.");
    }

    @Test
    public void writeDataInBatch_failure_seriveProblem_unspecifiedApiError() throws InfluxDBClientException {
        final List<Point> points = SampleData.getValidPointList(discriminator, timestampInMillis);

        doThrow(SampleAPIResponses.getHTTPErrorWithNoResponseBody())
            .when(influxDB).write(any());

        assertThatThrownBy(() -> influxDBOperations.writeDataInBatch(points))
            .isInstanceOf(InfluxDBOperationException.DataManipulationException.DataWriteOperationException.class)
            .hasMessage("The operation failed to successfully execute because: The service returned '500 Internal Error'. Could not write 3 data points.");
    }

    @Test
    public void writeDataInBatch_failure_connectionProblem_refused() throws InfluxDBClientException {
        final List<Point> points = SampleData.getValidPointList(discriminator, timestampInMillis);

        doThrow(RetrofitError.networkError("http://localhost:8086", new SocketException())).
            when(influxDB).write(any());

        assertThatExceptionOfType(InfluxDBConnectionException.RefusedException.class)
            .isThrownBy(() -> { influxDBOperations.writeDataInBatch(points); })
            .withMessage("The InfluxDB instance at http://localhost:8086 is unreachable. The connection was refused.");
    }

    @Test
    public void writeDataInBatch_failure_connectionProblem_timedOut() throws InfluxDBClientException {
        final List<Point> points = SampleData.getValidPointList(discriminator, timestampInMillis);

        doThrow(RetrofitError.networkError("http://localhost:8086", new SocketTimeoutException())).
            when(influxDB).write(any());

        assertThatThrownBy(() -> influxDBOperations.writeDataInBatch(points))
                .isInstanceOf(InfluxDBConnectionException.TimedOutException.class)
                .hasMessage("The InfluxDB instance at http://localhost:8086 is unreachable. The connection timed out.");
    }

    @Test
    public void writeDataInBatch_failure_connectionProblem_otherNetworkError() throws InfluxDBClientException {
        final List<Point> points = SampleData.getValidPointList(discriminator, timestampInMillis);

        doThrow(RetrofitError.networkError("http://localhost:8086", new IOException())).
            when(influxDB).write(any());

        assertThatThrownBy(() -> influxDBOperations.writeDataInBatch(points))
            .isInstanceOf(InfluxDBConnectionException.class)
            .hasMessage("The InfluxDB instance at http://localhost:8086 is unreachable.");
    }

    // Tests: createDatabase
    @Test
    public void createDatabase_success_validParams() throws InfluxDBClientException {
        // Operation
        influxDBOperations.createDatabase();
    }

    @Test
    public void createDatabase_failure_seriveProblem_wellFormedValidApiError() throws InfluxDBClientException {
        doThrow(SampleAPIResponses.getHTTPErrorWithWellFormedValidResponseBody("Bad database name"))
            .when(influxDB).createDatabase(any());

        assertThatThrownBy(() -> influxDBOperations.createDatabase())
            .isInstanceOf(InfluxDBOperationException.DataDefinitionException.DatabaseCreationException.class)
            .hasMessage("The operation failed to successfully execute because: The service returned '500 Internal Error - Bad database name'. Could not create a database with the name 'metrics'.");
    }

    @Test
    public void createDatabase_failure_seriveProblem_wellFormedInvalidApiError() throws InfluxDBClientException {
        doThrow(SampleAPIResponses.getHTTPErrorWithWellFormedInvalidResponseBody("Bad database name"))
            .when(influxDB).createDatabase(any());

        assertThatThrownBy(() -> influxDBOperations.createDatabase())
            .isInstanceOf(InfluxDBOperationException.DataDefinitionException.DatabaseCreationException.class)
            .hasMessage("The operation failed to successfully execute because: The service returned '500 Internal Error - {\"x\":\"Bad database name\"}'. Could not create a database with the name 'metrics'.");
    }

    @Test
    public void createDatabase_failure_seriveProblem_badlyFormedApiError() throws InfluxDBClientException {
        doThrow(SampleAPIResponses.getHTTPErrorWithBadlyFormedResponseBody("Bad database name"))
            .when(influxDB).createDatabase(any());

        assertThatThrownBy(() -> influxDBOperations.createDatabase())
            .isInstanceOf(InfluxDBOperationException.DataDefinitionException.DatabaseCreationException.class)
            .hasMessage("The operation failed to successfully execute because: The service returned '500 Internal Error - Bad database name'. Could not create a database with the name 'metrics'.");
    }

    @Test
    public void createDatabase_failure_seriveProblem_unspecifiedApiError() throws InfluxDBClientException {
        doThrow(SampleAPIResponses.getHTTPErrorWithNoResponseBody())
            .when(influxDB).createDatabase(any());

        assertThatThrownBy(() -> influxDBOperations.createDatabase())
            .isInstanceOf(InfluxDBOperationException.DataDefinitionException.DatabaseCreationException.class)
            .hasMessage("The operation failed to successfully execute because: The service returned '500 Internal Error'. Could not create a database with the name 'metrics'.");
        }

    @Test
    public void createDatabase_failure_connectionProblem_refused() throws InfluxDBClientException {
        doThrow(RetrofitError.networkError("http://localhost:8086", new SocketException())).
            when(influxDB).createDatabase(any());

        assertThatThrownBy(() -> influxDBOperations.createDatabase())
            .isInstanceOf(InfluxDBConnectionException.RefusedException.class)
            .hasMessage("The InfluxDB instance at http://localhost:8086 is unreachable. The connection was refused.");
    }

    @Test
    public void createDatabase_failure_connectionProblem_timedOut() throws InfluxDBClientException {
        doThrow(RetrofitError.networkError("http://localhost:8086", new SocketTimeoutException())).
            when(influxDB).createDatabase(any());

        assertThatThrownBy(() -> influxDBOperations.createDatabase())
            .isInstanceOf(InfluxDBConnectionException.TimedOutException.class)
            .hasMessage("The InfluxDB instance at http://localhost:8086 is unreachable. The connection timed out.");
    }

    @Test
    public void createDatabase_failure_connectionProblem_otherNetworkError() throws InfluxDBClientException {
        doThrow(RetrofitError.networkError("http://localhost:8086", new IOException())).
            when(influxDB).createDatabase(any());

        assertThatThrownBy(() -> influxDBOperations.createDatabase())
            .isInstanceOf(InfluxDBConnectionException.class)
            .hasMessage("The InfluxDB instance at http://localhost:8086 is unreachable.");
    }
}
