package com.barchart.netty.host.api;

import io.netty.channel.Channel;

/** represents netty pipeline builder */
public interface NettyPipe {

	enum Mode {

		/** default or parent */
		DEFAULT, //

		/** derived or child */
		DERIVED, //

	}

	/** UUID of this pipeline builder */
	String type();

	/**
	 * build a new pipeline and apply it to the channel
	 * <p>
	 * owner net-point will be attached as {@link NettyDot#ATTR_NET_POINT}
	 */
	void apply(Channel channel, Mode mode);

}
