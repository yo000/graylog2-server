/*
 * Copyright (C) 2020 Graylog, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
package org.graylog.datanode.management;

import com.google.common.util.concurrent.AbstractIdleService;
import org.graylog.datanode.process.OpensearchConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class OpensearchProcessService extends AbstractIdleService implements Provider<OpensearchProcess> {

    private static final int WATCHDOG_RESTART_ATTEMPTS = 3;
    private final OpensearchProcess process;

    @Inject
    public OpensearchProcessService(OpensearchConfiguration config, @Named(value = "process_logs_buffer_size") int logsSize) {
        this.process = createOpensearchProcess(config, logsSize);
    }

    private OpensearchProcess createOpensearchProcess(OpensearchConfiguration config, int logsSize) {
        final OpensearchProcessImpl process = new OpensearchProcessImpl(config, logsSize);
        final ProcessWatchdog watchdog = new ProcessWatchdog(process, WATCHDOG_RESTART_ATTEMPTS);
        process.setStateMachineTracer(watchdog);
        return process;
    }

    @Override
    protected void startUp() {
        // todo: this should be moved to a configuration provider that will know the right moment when to start the
        // opensearch process.
        this.process.startWithConfig(new OpensearchDynamicConfiguration());
    }


    @Override
    protected void shutDown() {
        this.process.stop();
    }

    @Override
    public OpensearchProcess get() {
        return process;
    }
}
