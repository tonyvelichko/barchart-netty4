/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.impl;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.netty.api.NettyDot;
import com.barchart.netty.api.NettyDotManager;
import com.barchart.netty.api.NettyGroup;
import com.barchart.netty.util.point.NetPoint;
import com.barchart.osgi.factory.api.Cidget;
import com.barchart.osgi.factory.api.CidgetManagerBase;
import com.typesafe.config.Config;

/**
 * dot factory manager
 * 
 * FIXME get thread pool params from config admin
 */
@Component(immediate = true)
public class NettyDotProvider extends CidgetManagerBase<NettyDot> implements
		NettyDotManager, NettyGroup {

	static {
		new NettySetup();
	}

	protected final Logger log = LoggerFactory.getLogger(getClass());

	//

	private final Timer timer = new HashedWheelTimer();

	public Timer getTimer() {
		return timer;
	}

	//

	@Override
	@Activate
	protected void activate(final ComponentContext c) {

		super.activate(c);

		log.debug("@@@ ACTIVE");

		// threadPool = threadPoolManager.get(POOL_NAME);

		group = new NioEventLoopGroup(10, threadFactory);

	}

	@Override
	@Deactivate
	protected void deactivate(final ComponentContext c) {

		log.debug("@@@ INACTIVE");

		// threadPoolManager.release(threadPool);

		group.shutdown();

		super.deactivate(c);

	}

	@Override
	protected Class<NettyDot> interfaceClass() {
		return NettyDot.class;
	}

	// private static final String POOL_NAME = NettyManagerProvider.class
	// .getName();
	// private ThreadPool threadPool;
	// private ThreadPoolManager threadPoolManager;
	// @Reference
	// protected void bind(final ThreadPoolManager s) {
	// threadPoolManager = s;
	// }
	// protected void unbind(final ThreadPoolManager s) {
	// threadPoolManager = null;
	// }

	private final ThreadFactory threadFactory = new NettyThreadFactory();

	private EventLoopGroup group;

	@Override
	public EventLoopGroup getGroup() {
		return group;
	}

	public static Map<String, String> props(final Config config) {

		final Map<String, String> props = new HashMap<String, String>();

		final String configuration = config.root().render();

		props.put(Cidget.PROP_COMPONENT_CONFIG, configuration);

		return props;
	}

	@Override
	public NettyDot create(final Config config) {

		final String factoryId = config.getString(NetPoint.KEY_TYPE);
		final String instanceId = config.getString(NetPoint.KEY_ID);

		return instanceCreate(factoryId, instanceId, props(config));

	}

	@Override
	public boolean destroy(final Config config) {

		final String instanceId = config.getString(NetPoint.KEY_ID);

		return instanceDestroy(instanceId);

	}

	@Override
	public boolean update(final Config config) {

		final String instanceId = config.getString(NetPoint.KEY_ID);

		return instanceUpdate(instanceId, props(config));

	}

}
