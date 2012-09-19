package com.barchart.netty.test.echo_msg;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.host.api.NettyDot;
import com.barchart.netty.part.hand.SctpMessageCodec;
import com.barchart.netty.part.pipe.PipeAny;

/**  */
@Component(name = PipeEchoMsgServer.NAME, immediate = true)
public class PipeEchoMsgServer extends PipeAny {

	public static final String NAME = "barchart.netty.pipe.echo.message.server";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected void applyDefault(final NettyDot dot, final Channel channel) {

		log.debug("apply parent : {}", channel);

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new LoggingHandler());

	}

	@Override
	protected void applyDerived(final NettyDot dot, final Channel channel) {

		log.debug("apply child : {}", channel);

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast("logger", new LoggingHandler());

		pipeline.addLast("sctp-codec", new SctpMessageCodec());

		pipeline.addLast("echo-server", new HandEchoMsgServer());

	}

}