/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package temp.arb;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.MessageLoggingHandler;

import org.osgi.service.component.annotations.Component;

import com.barchart.netty.part.hand.BlackHoleMessageHandler;
import com.barchart.netty.pipe.PipeAny;

/** use for arbiter source end points */
@Component(name = PipeArbSource.TYPE, immediate = true)
public class PipeArbSource extends PipeAny implements NameArb {

	public static final String TYPE = "barchart.netty.pipe.arbiter.source";

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public void apply(final Channel channel, final Mode mode) {

		final ChannelPipeline pipeline = channel.pipeline();

		pipeline.addLast(LOGGER, new MessageLoggingHandler());

		/** place holder for message redirect */
		pipeline.addLast(ARBITER, new BlackHoleMessageHandler());

	}

}
