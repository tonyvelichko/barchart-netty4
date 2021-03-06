/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.part.hand;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundMessageHandler;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.api.NettyDot;
import com.barchart.netty.util.point.NetPoint;

/** from ByteBuf into DatagramPacket, final stage before write */
public class DatagramPacketWriter extends ChannelDuplexHandler implements
		ChannelOutboundMessageHandler<Object> {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected InetSocketAddress localAddress;
	protected InetSocketAddress remoteAddress;

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {

		final NetPoint point = ctx.channel().attr(NettyDot.ATTR_NET_POINT)
				.get();

		localAddress = point.getLocalAddress();
		remoteAddress = point.getRemoteAddress();

		super.channelActive(ctx);

	}

	@Override
	public MessageBuf<Object> newOutboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer();
	}

	@Override
	public void flush(final ChannelHandlerContext ctx,
			final ChannelPromise promise) throws Exception {

		final MessageBuf<Object> source = ctx.outboundMessageBuffer();

		final MessageBuf<Object> target = ctx.nextOutboundMessageBuffer();

		while (true) {

			final Object entry = source.poll();

			if (entry == null) {
				break;
			}

			if (entry instanceof ByteBuf) {

				final ByteBuf buffer = (ByteBuf) entry;

				final DatagramPacket packet = new DatagramPacket(buffer,
						remoteAddress);

				target.add(packet);

			} else {

				target.add(entry);

			}

		}

		ctx.flush(promise);

	}

	@Override
	public void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {
		// TODO Auto-generated method stub
		ctx.fireInboundBufferUpdated();
	}

}
