/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.boot;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.api.NettyBoot;
import com.barchart.netty.api.NettyDot;
import com.barchart.netty.api.NettyGroup;
import com.barchart.netty.api.NettyPipe;
import com.barchart.netty.api.NettyPipeManager;
import com.barchart.netty.util.point.NetPoint;

public abstract class BootAny implements NettyBoot {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Activate
	protected void bootActivate() {
		log.debug("@@@ activate : {}", type());
	}

	@Deactivate
	protected void bootDeactivate() {
		log.debug("@@@ deactivate : {}", type());
	}

	@Override
	public abstract ChannelFuture startup(final NetPoint netPoint)
			throws Exception;

	@Override
	public ChannelFuture shutdown(final NetPoint netPoint, final Channel channel)
			throws Exception {

		return channel.close();

	}

	/**
	 * builder for transient pipeline applicator handler
	 */
	protected final ChannelInitializer<Channel> pipeApply(
			final NetPoint netPoint, final NettyPipe.Mode mode) {

		return new ChannelInitializer<Channel>() {
			@Override
			public void initChannel(final Channel channel) throws Exception {

				/** always link channel with owner end point */
				// FIXME why it was commented out
				channel.attr(NettyDot.ATTR_NET_POINT).set(netPoint);

				final NettyPipe pipe = pipeManager().findPipe(
						netPoint.getPipeline(), netPoint.getPipelineTimeout(),
						TimeUnit.MILLISECONDS);

				if (pipe == null) {
					log.error("missing pipeline", new IllegalArgumentException(
							netPoint.getPipeline()));
				} else {
					pipe.apply(channel, mode);
				}

			}
		};

	}

	private EventLoopGroup group;

	protected EventLoopGroup group() {
		return group;
	}

	@Reference
	protected void bind(final NettyGroup s) {
		group = s.getGroup();
	}

	protected void unbind(final NettyGroup s) {
		group = null;
	}

	private NettyPipeManager pipeManager;

	protected NettyPipeManager pipeManager() {
		return pipeManager;
	}

	@Reference
	protected void bind(final NettyPipeManager pm) {
		pipeManager = pm;
	}

	protected void unbind(final NettyPipeManager pm) {
		pipeManager = null;
	}

}
