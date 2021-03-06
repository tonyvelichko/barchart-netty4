/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.test.echo_msg;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.part.hand.SctpMessageCodec;
import com.barchart.netty.pipe.PipeAny;

/**  */
@Component(name = PipeEchoMsgClient.TYPE, immediate = true)
public class PipeEchoMsgClient extends PipeAny {

	public static final String TYPE = "barchart.netty.pipe.echo.message.client";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public void apply(final Channel channel, final Mode mode) {

		log.debug("apply client : {}", channel);

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new LoggingHandler());

		pipeline.addLast("sctp-codec", new SctpMessageCodec());

		pipeline.addLast("echo-client", new HandEchoMsgClient(128));

	}

}
