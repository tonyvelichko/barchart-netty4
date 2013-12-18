package com.barchart.netty.client.example;

import io.netty.channel.EventLoopGroup;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rx.util.functions.Action1;

import com.barchart.netty.client.Connectable;
import com.barchart.netty.client.base.ConnectableBase;
import com.barchart.netty.client.base.ConnectableProxy;
import com.barchart.netty.client.facets.AuthenticationAware;
import com.barchart.netty.client.facets.AuthenticationAware.AuthState;
import com.barchart.netty.client.facets.AuthenticationFacet;
import com.barchart.netty.client.facets.KeepaliveFacet;
import com.barchart.netty.client.facets.LatencyAware;
import com.barchart.netty.client.facets.SecureAware;
import com.barchart.netty.client.facets.SecureFacet;
import com.barchart.netty.client.policy.ReconnectPolicy;
import com.barchart.netty.client.transport.TransportProtocol;

/**
 * Example of ExampleClient if it was implemented as a dynamic proxy.
 */
public class ExampleProxyClient extends ConnectableProxy<ExampleProxyClient> {

	interface BarchartClient extends Connectable<BarchartClient>, SecureAware,
			AuthenticationAware<Object>, LatencyAware {

		void sendMessage(Object msg);

	}

	protected static class Builder extends
			ConnectableBase.Builder<Builder, ExampleProxyClient> {

		@Override
		public ExampleProxyClient build() {

			final ExampleProxyClient client =
					new ExampleProxyClient(eventLoop, transport);

			return super.configure(client);

		}

	}

	public static Builder builder() {
		return new Builder();
	}

	protected ExampleProxyClient(final EventLoopGroup eventLoop_,
			final TransportProtocol transport_) {

		super(eventLoop_, transport_);

	}

	/**
	 ** USAGE EXAMPLE
	 **/

	public static void main(final String[] args) {

		// Base configuration
		final ExampleProxyClient client =
				ExampleProxyClient.builder().host("tcp://localhost:6497")
						.timeout(30, TimeUnit.SECONDS).build();

		// Add dynamic facets
		client.facet(new SecureFacet(SecureFacet.Encryption.REFUSE));
		client.facet(new KeepaliveFacet(10, TimeUnit.SECONDS));
		client.facet(new AuthenticationFacet<Object>(
				new DummyAuthenticationHandler.Builder()));

		// Construct with final interface type
		final BarchartClient proxy = client.proxy(BarchartClient.class);

		// Reconnection policy
		proxy.stateChanges().subscribe(
				new ReconnectPolicy(Executors.newScheduledThreadPool(1), 5,
						TimeUnit.SECONDS));

		proxy.stateChanges().subscribe(statePrinter);
		proxy.authStateChanges().subscribe(authPrinter);

		client.connect();

	}

	final static Action1<AuthState> authPrinter = new Action1<AuthState>() {
		@Override
		public void call(final AuthState event) {
			System.out.println(event.getClass().getSimpleName() + " event: "
					+ event.toString());
		}
	};

	final static Action1<StateChange<?>> statePrinter =
			new Action1<StateChange<?>>() {
				@Override
				public void call(final StateChange<?> event) {
					System.out.println(event.state().getClass().getSimpleName()
							+ " event: " + event.state().toString());
				}
			};

}
