/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.server;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;

import java.net.SocketAddress;

public interface Server<T extends Server<T>> {

	/**
	 * Start a server on the specified port and hostname (or IP).
	 */
	ChannelFuture listen(final int port, final String hostOrIp);

	/**
	 * Start a server on the specified address.
	 */
	ChannelFuture listen(final SocketAddress address);

	/**
	 * Shutdown the server gracefully. This does not kill active client
	 * connections.
	 */
	Future<T> shutdown();

	/**
	 * Shutdown the server and kill all active client connections.
	 */
	Future<T> kill();

	/**
	 * Return a future for the server shutdown process.
	 */
	Future<T> shutdownFuture();

	/**
	 * Check if the server is currently running.
	 */
	boolean running();

}