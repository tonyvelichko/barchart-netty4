/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.common.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;

import com.barchart.netty.common.messages.Ping;
import com.barchart.netty.common.messages.Pong;
import com.barchart.netty.common.metadata.LatencyAware;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.MetricsRegistry;

public class PingHandler extends SimpleChannelInboundHandler<Object> implements
		LatencyAware {

	// Latency sample histogram
	private final Histogram latencySampler = new MetricsRegistry()
			.newHistogram(getClass(), "peer-latency", true);

	// Latest peer clock / latency measurements
	private long clockSkew = 0;
	private long latency = 0;

	private ScheduledFuture<?> pingFuture = null;

	private long interval = 0;
	private TimeUnit unit = TimeUnit.SECONDS;

	public PingHandler() {
	}

	public PingHandler(final long interval_, final TimeUnit unit_) {
		interval(interval_, unit_);
	}

	public void interval(final long interval_, final TimeUnit unit_) {
		interval = interval_;
		unit = unit_;
	}

	@Override
	public long clockSkew() {
		return clockSkew;
	}

	@Override
	public long latency() {
		return latency;
	}

	@Override
	public double averageLatency() {
		return latencySampler.mean();
	}

	@Override
	public long peerTime() {
		return System.currentTimeMillis() + clockSkew;
	}

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, final Object msg)
			throws Exception {

		if (msg instanceof Ping) {

			ctx.write(new Pong() {

				@Override
				public long timestamp() {
					return System.currentTimeMillis();
				}

				@Override
				public long pinged() {
					return ((Ping) msg).timestamp();
				}

			});
			ctx.flush();

		} else if (msg instanceof Pong) {

			final Pong pong = (Pong) msg;

			// Compare the peer-received timestamp to current
			final long now = System.currentTimeMillis();
			latency = (now - pong.pinged()) / 2;
			clockSkew = pong.timestamp() - (now - latency);
			latencySampler.update(latency);

		}

		// Send upstream
		ctx.fireChannelRead(msg);

	}

	@Override
	public boolean acceptInboundMessage(final Object msg) throws Exception {
		return (msg instanceof Ping || msg instanceof Pong);
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {

		startPing(ctx);

		ctx.fireChannelActive();

	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx)
			throws Exception {

		stopPing();

		ctx.fireChannelInactive();

	}

	private void startPing(final ChannelHandlerContext ctx) {

		synchronized (this) {

			if (pingFuture != null && !pingFuture.isDone()) {
				pingFuture.cancel(false);
			}

			if (interval > 0) {
				pingFuture = ctx.channel().eventLoop().schedule(new Runnable() {
					@Override
					public void run() {
						sendPing(ctx);
					}
				}, interval, unit);
			}

		}

	}

	private void stopPing() {

		synchronized (this) {

			if (pingFuture != null) {

				if (!pingFuture.isDone()) {
					pingFuture.cancel(false);
				}

				pingFuture = null;

			}

		}

	}

	public void sendPing(final ChannelHandlerContext ctx) {

		try {

			if (pingFuture != null && !pingFuture.isDone()) {
				pingFuture.cancel(false);
			}

			ctx.write(new Ping() {

				@Override
				public long timestamp() {
					return System.currentTimeMillis();
				}

			});
			ctx.flush();

		} finally {

			if (interval > 0) {
				pingFuture = ctx.channel().eventLoop().schedule(new Runnable() {
					@Override
					public void run() {
						sendPing(ctx);
					}
				}, interval, unit);
			}

		}

	}

}
