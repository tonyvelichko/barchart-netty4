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
import io.netty.channel.ChannelInboundMessageHandler;
import io.netty.channel.ChannelOutboundMessageHandler;
import io.netty.channel.ChannelPromise;
import io.netty.channel.sctp.SctpMessage;

/** ByteBuf-SctpData wrapper */
public class SctpMessageCodec extends ChannelDuplexHandler implements
		ChannelInboundMessageHandler<Object>,
		ChannelOutboundMessageHandler<Object> {

	@Override
	public MessageBuf<Object> newOutboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer();
	}

	@Override
	public MessageBuf<Object> newInboundBuffer(final ChannelHandlerContext ctx)
			throws Exception {
		return Unpooled.messageBuffer();
	}

	@Override
	public void inboundBufferUpdated(final ChannelHandlerContext ctx)
			throws Exception {

		final MessageBuf<Object> source = ctx.inboundMessageBuffer();

		final MessageBuf<Object> target = ctx.nextInboundMessageBuffer();

		while (true) {

			final Object entry = source.poll();

			if (entry == null) {
				break;
			}

			if (entry instanceof SctpMessage) {

				final SctpMessage data = (SctpMessage) entry;

				final ByteBuf buffer = data.content();

				target.add(buffer);

			}

		}

		ctx.fireInboundBufferUpdated();

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

				final SctpMessage data = new SctpMessage(0, 0, (ByteBuf) entry);

				target.add(data);
			}

		}

		ctx.flush(promise);

	}

}
