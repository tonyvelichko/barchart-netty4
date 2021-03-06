/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.dot;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.barchart.netty.api.NettyBoot;
import com.barchart.netty.boot.BootCastMulti;

/**
 * parent for multicast end points
 * 
 * handles multicast join / leave;
 */
@Component(name = DotCastMulti.TYPE, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DotCastMulti extends DotCast {

	public static final String TYPE = "barchart.netty.dot.cast.multi";

	@Override
	protected NettyBoot boot() {
		return bootManager().findBoot(BootCastMulti.TYPE);
	}

}
