package com.barchart.netty.part.dot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSctpChannel;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.host.api.NettyPipe;

/**
 * parent for connection oriented client end points
 * 
 * such as SCTP
 */
@Component(name = DotStormClient.TYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DotStormClient extends DotAny {

	public static final String TYPE = "barchart.netty.dot.storm.client";

	private Bootstrap boot;

	protected Bootstrap boot() {
		return boot;
	}

	private NioSctpChannel channel;

	@Override
	public NioSctpChannel channel() {
		return channel;
	}

	protected ChannelFuture activateFuture;
	protected ChannelFuture deactivateFuture;

	@Override
	protected void activateBoot() throws Exception {

		boot = new Bootstrap();
		channel = new NioSctpChannel();

		boot().localAddress(localAddress());
		boot().remoteAddress(remoteAddress());

		boot().option(ChannelOption.SCTP_NODELAY, true);

		/** https://github.com/netty/netty/issues/610 */
		boot().option(ChannelOption.SO_SNDBUF, //
				netPoint().getSendBufferSize());
		boot().option(ChannelOption.SO_RCVBUF, //
				netPoint().getReceiveBufferSize());

		boot().group(group());

		boot().channelFactory(new FixedChannelFactory(channel()));

		/** connector */
		boot().handler(pipeApply(NettyPipe.Mode.DEFAULT));

		activateFuture = boot().connect();

	}

	@Override
	protected void deactivateBoot() throws Exception {

		deactivateFuture = channel().close();

		channel = null;
		boot = null;

	}

}
