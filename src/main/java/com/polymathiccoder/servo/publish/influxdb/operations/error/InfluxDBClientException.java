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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polymathiccoder.servo.publish.influxdb.operations.InfluxDBConfiguration;

@SuppressWarnings("serial")
public abstract class InfluxDBClientException extends RuntimeException {

    protected static final Logger LOGGER = LoggerFactory.getLogger(InfluxDBClientException.class);

    protected final WeakReference<InfluxDBConfiguration> influxDBConfiguration;

    protected String reason;

    public InfluxDBClientException(final InfluxDBConfiguration influxDBConfiguration) {
        this.influxDBConfiguration = new WeakReference<>(influxDBConfiguration);
        reason = "";
    }

    @Override
    public abstract String getMessage();

    public void setReason(String reason) {
        this.reason = reason;
    }
}