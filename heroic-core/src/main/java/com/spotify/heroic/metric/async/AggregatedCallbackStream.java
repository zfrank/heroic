package com.spotify.heroic.metric.async;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.ImmutableList;
import com.spotify.heroic.aggregation.Aggregation;
import com.spotify.heroic.aggregation.Aggregation.Group;
import com.spotify.heroic.metric.model.FetchData;
import com.spotify.heroic.metric.model.ResultGroup;
import com.spotify.heroic.metric.model.ResultGroups;
import com.spotify.heroic.metric.model.TagValues;
import com.spotify.heroic.model.DataPoint;
import com.spotify.heroic.model.Statistics;
import com.spotify.heroic.model.TimeData;

import eu.toolchain.async.AsyncFuture;
import eu.toolchain.async.StreamCollector;

@Slf4j
@RequiredArgsConstructor
public class AggregatedCallbackStream<T extends TimeData> implements StreamCollector<FetchData<T>, ResultGroups> {
    private final List<TagValues> group;
    private final Aggregation.Session session;
    private final List<? extends AsyncFuture<?>> fetches;

    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    @Override
    public void resolved(FetchData<T> result) throws Exception {
        session.update(new Aggregation.Group(result.getSeries().getTags(), result.getData()));
    }

    @Override
    public void failed(Throwable error) throws Exception {
        log.error("Request failed: " + error.toString(), error);
        cancelRest();
    }

    @Override
    public void cancelled() throws Exception {
        cancelRest();
    }

    private void cancelRest() {
        if (!cancelled.compareAndSet(false, true))
            return;

        for (final AsyncFuture<?> future : fetches)
            future.cancel();
    }

    @Override
    public ResultGroups end(int successful, int failed, int cancelled) throws Exception {
        if (failed > 0 || cancelled > 0)
            throw new Exception("Some series were not fetched from the database");

        final Aggregation.Result result = session.result();

        if (result.getResult().size() != 1)
            throw new Exception("only expected one result group");

        final Group g = result.getResult().iterator().next();

        final Statistics stat = Statistics.builder().aggregator(result.getStatistics())
                .row(new Statistics.Row(successful, failed)).build();

        final ResultGroup resultGroup = new ResultGroup(this.group, g.getValues(), DataPoint.class);
        return ResultGroups.fromResult(ImmutableList.of(resultGroup), stat);
    }
}