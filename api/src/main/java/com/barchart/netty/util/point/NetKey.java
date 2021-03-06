/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.util.point;

/**
 * Standard properties of network point {@link NetPoint}.
 */
public interface NetKey {

	/** end point uuid */
	String KEY_ID = "id";

	/** end point type (dot factory id) */
	String KEY_TYPE = "type";

	/** pipeline name (pipe component name ) */
	String KEY_PIPELINE = "pipeline";

	/** Time to wait for the pipeline to appear via OSGI injection */
	String KEY_PIPELINE_TIMEOUT = "pipeline-timeout";

	String KEY_LOCAL_ADDRESS = "local-address";

	String KEY_REMOTE_ADDRESS = "remote-address";

	String KEY_PACKET_TTL = "packet-ttl";

	String KEY_RECV_BUF_SIZE = "receive-buffer-size";

	String KEY_SEND_BUF_SIZE = "send-buffer-size";

}
