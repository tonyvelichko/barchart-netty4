/**
 * Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.netty.client.facets;

import io.netty.channel.ChannelPipeline;
import rx.Observable;
import rx.subjects.PublishSubject;

import com.barchart.netty.client.pipeline.AuthenticationHandler;
import com.barchart.netty.client.pipeline.CapabilitiesRequest;
import com.barchart.netty.common.metadata.AuthenticationAware;

/**
 * A Connectable proxy facet that implements the AuthenticationAware interface.
 * The actual authentication communication is implementation-dependent via a
 * provided AuthenticationHandler. MessageFlowHandler is a recommended base
 * class for writing authentication handlers in order to block downstream
 * handlers from receiving channelActive() events until authentication is
 * complete.
 *
 * @param <A> The account object type
 */
// Can't return a parameterized class object so need to make it the base type
@SuppressWarnings("rawtypes")
public class AuthenticationFacet<A> implements
		ConnectableFacet<AuthenticationAware>, AuthenticationAware<A> {

	// Relay to manage subscribers, since the underlying handler can
	// change between connect attempts
	private final PublishSubject<AuthState> stateRelay = PublishSubject
			.create();

	private AuthenticationHandler<A> authenticator = null;

	private final AuthenticationHandler.Builder<A> builder;

	public AuthenticationFacet(final AuthenticationHandler.Builder<A> builder_) {
		builder = builder_;
	}

	@Override
	public Class<AuthenticationAware> type() {
		return AuthenticationAware.class;
	}

	@Override
	public void initPipeline(final ChannelPipeline pipeline) throws Exception {

		if (pipeline.get(CapabilitiesRequest.class) == null)
			pipeline.addLast(new CapabilitiesRequest());

		authenticator = builder.build();
		authenticator.authStateChanges().subscribe(stateRelay);

		pipeline.addLast(authenticator);

	}

	@Override
	public Observable<AuthState> authStateChanges() {
		return stateRelay;
	}

	@Override
	public AuthState authState() {

		if (authenticator == null) {
			return AuthState.NOT_AUTHENTICATED;
		}

		return authenticator.authState();

	}

	@Override
	public A account() {

		if (authenticator == null) {
			return null;
		}

		return authenticator.account();

	}

}
