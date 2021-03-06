/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server.stream;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.NetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;

import com.barchart.netty.server.base.AbstractServer;

/**
 * A multicast transceiver that can send and receive messages in a UDP multicast
 * group.
 */
public class MulticastTransceiver extends
		AbstractServer<MulticastTransceiver, Bootstrap> {

	protected InetSocketAddress multicast;

	/**
	 * The multicast group address.
	 */
	public MulticastTransceiver multicast(final InetSocketAddress address) {
		multicast = address;
		return this;
	}

	@Override
	protected Bootstrap bootstrap() {

		final Bootstrap bootstrap = new Bootstrap() //
				.group(defaultGroup) //
				.channel(NioDatagramChannel.class) //
				.handler(new ServerChannelInitializer()) //
				.option(ChannelOption.SO_REUSEADDR, true) //
				.option(ChannelOption.IP_MULTICAST_TTL, 3) //
				.option(ChannelOption.SO_SNDBUF, 262144) //
				.option(ChannelOption.SO_RCVBUF, 262144);

		if (bootstrapInit != null) {
			bootstrapInit.initBootstrap(bootstrap);
		}

		return bootstrap;

	}

	/**
	 * Join the multicast group address using the network interface associated
	 * with the given address.
	 */
	@Override
	public ChannelFuture listen(final SocketAddress address) {

		if (pipelineInit == null) {
			throw new IllegalStateException(
					"No pipeline initializer has been provided, server would do nothing");
		}

		// Kinda hacky, need to override bootstrap params based on passed
		// address

		final ChannelFuture future =
				bootstrap() //
						.option(ChannelOption.IP_MULTICAST_IF,
								bindInterface((InetSocketAddress) address)) //
						.localAddress(multicast.getPort()) //
						.remoteAddress(multicast) //
						.bind();

		future.addListener(new GenericFutureListener<ChannelFuture>() {

			@Override
			public void operationComplete(final ChannelFuture future)
					throws Exception {

				if (future.isSuccess()) {

					final NioDatagramChannel channel =
							(NioDatagramChannel) future.channel();

					channel.joinGroup(
							multicast,
							channel.config().getOption(
									ChannelOption.IP_MULTICAST_IF));

				}

			}

		});

		serverChannels.add(future.channel());

		return future;

	}

	@Override
	public Future<MulticastTransceiver> shutdown() {

		for (final Channel c : serverChannels) {

			final NioDatagramChannel dc = (NioDatagramChannel) c;

			final InetSocketAddress multicast = dc.remoteAddress();

			dc.leaveGroup(multicast,
					dc.config().getOption(ChannelOption.IP_MULTICAST_IF));

		}

		return super.shutdown();

	}

	/**
	 * Valid interface or loop back interface for error.
	 */
	protected NetworkInterface bindInterface(final InetSocketAddress local) {

		try {

			final InetAddress address = local.getAddress();

			final NetworkInterface iface =
					NetworkInterface.getByInetAddress(address);

			if (iface == null) {
				throw new IllegalArgumentException(
						"address is not assigned to any iterface : " + address);
			}

			return iface;

		} catch (final Throwable e) {
			return NetUtil.LOOPBACK_IF;
		}

	}

}
