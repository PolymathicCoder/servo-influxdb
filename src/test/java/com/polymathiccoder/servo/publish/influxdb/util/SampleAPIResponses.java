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

/*-
 * #%L
 * SampleAPIResponses.java - servo-influxdb - PolymathicCoder LLC - 2,016
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



import java.lang.reflect.Type;
import java.util.ArrayList;

import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import retrofit.mime.TypedString;

public class SampleAPIResponses {
    public static RetrofitError getHTTPErrorWithWellFormedValidResponseBody(final String errorMessage) {
        return RetrofitError.httpError(
                "http://localhost:8086",
                new Response(
                        "http://localhost:8086",
                        500, "Internal Error",
                        new ArrayList<>(0),
                        new TypedString("{\"error\": \""+ errorMessage + "\"}")),
                new Converter() {
                    @Override
                    public TypedOutput toBody(final Object object) {
                        return new TypedString(object.toString());
                    }

                    @Override
                    public Object fromBody(final TypedInput body, final Type type) throws ConversionException {
                        return body.toString();
                    }
                },
                String.class);
    }

    public static RetrofitError getHTTPErrorWithBadlyFormedResponseBody(final String errorMessage) {
        return RetrofitError.httpError(
                "http://localhost:8086",
                new Response(
                        "http://localhost:8086",
                        500, "Internal Error",
                        new ArrayList<>(0),
                        new TypedString(errorMessage)),
                new Converter() {
                    @Override
                    public TypedOutput toBody(final Object object) {
                        return new TypedString(object.toString());
                    }

                    @Override
                    public Object fromBody(final TypedInput body, final Type type) throws ConversionException {
                        return body.toString();
                    }
                },
                String.class);
    }

    public static RetrofitError getHTTPErrorWithWellFormedInvalidResponseBody(final String errorMessage) {
        return RetrofitError.httpError(
                "http://localhost:8086",
                new Response(
                        "http://localhost:8086",
                        500, "Internal Error",
                        new ArrayList<>(0),
                        new TypedString("{\"x\": \""+ errorMessage + "\"}")),
                new Converter() {
                    @Override
                    public TypedOutput toBody(final Object object) {
                        return new TypedString(object.toString());
                    }

                    @Override
                    public Object fromBody(final TypedInput body, final Type type) throws ConversionException {
                        return body.toString();
                    }
                },
                String.class);
    }

    public static RetrofitError getHTTPErrorWithNoResponseBody() {
        return RetrofitError.httpError(
                "http://localhost:8086",
                new Response(
                        "http://localhost:8086",
                        500, "Internal Error",
                        new ArrayList<>(0),
                        null),
                new Converter() {
                    @Override
                    public TypedOutput toBody(final Object object) {
                        return new TypedString(object.toString());
                    }

                    @Override
                    public Object fromBody(final TypedInput body, final Type type) throws ConversionException {
                        return body.toString();
                    }
                },
                String.class);
    }
}
