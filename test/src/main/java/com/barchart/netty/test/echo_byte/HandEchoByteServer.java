/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.test.echo_byte;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundByteHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class HandEchoByteServer extends ChannelInboundByteHandlerAdapter {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void inboundBufferUpdated(final ChannelHandlerContext ctx,
			final ByteBuf source) {

		final ByteBuf target = ctx.nextOutboundByteBuffer();

		target.discardReadBytes();

		target.writeBytes(source);

		ctx.flush();

		printStatus();

	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx,
			final Throwable cause) {

		log.error("unexpected", cause);

		ctx.close();

	}

	private long count;

	private void printStatus() {

		if (count % 10000 == 0) {
			log.debug("server pong count = {}", count);
		}

		count++;

	}

}
