/*
 * Copyright (c) 2016 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.heroic.jetty;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Slf4jRequestLog;

@Slf4j
@RequiredArgsConstructor
public class JettyRequestLogHandler extends Slf4jRequestLog {

    @Override
    public void log(Request request, Response response) {
        try {
            write(request.getRemoteAddr() + " " +
                     "[" + request.getTimeStamp() + "] \"" +
                     request.getMethod() + " " +
                     request.getRequestURI() + "\" " +
                     response.getStatus() +
                     "\n---\n" +
                     request.getInputStream() +
                     "\n---\n" +
                     request.getQueryString() +
                     "\n---\n" +
                     request.toString() +
                     "\n---\n" +
                     response.toString() +
                     "\n---");
        } catch (Exception e) {
            // Ignore logging error, in the unlikely event..
        }
    }
}
