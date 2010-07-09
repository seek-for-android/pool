package android.smartcard.service;

/**
 * Implementation of a basic or logical card channel abstraction
 * managed by a <code>SeekClient</code> instance, connected to the SEEK service.
 */
final class CardChannel implements ICardChannel {

	private final SeekClient client;
	private final long hChannel;
	private final boolean isLogicalChannel;
	private volatile boolean isClosed;

	/**
	 * Constructs a new abstraction of a SEEK based card channel.
	 * @param client
	 *          the SEEK client instance to which this channel is associated.
	 * @param hChannel
	 *          the handle associated to this card channel by the SEEK service.
	 * @param isLogicalChannel
	 *          <code>true</code> if this is a logical channel, <code>false</code> if this is a basic channel.
	 */
	CardChannel(SeekClient client, long hChannel, boolean isLogicalChannel) {
		this.client = client;
		this.hChannel = hChannel;
		this.isLogicalChannel = isLogicalChannel;
	}

	/**
	 * Asserts that this channel is open.
	 * @throws IllegalStateException
	 *           if the channel is in closed state.
	 */
	private void assertOpen() {
		if (isClosed) {
			throw new IllegalStateException("channel is closed");
		}
	}

	@Override
	public void close() throws CardException {
		if (isClosed)
			return;
		try {
			client.closeChannel(this);
		} catch (IllegalArgumentException ignore) {
		} catch (IllegalStateException ignore) {
		}
	}

	@Override
	public byte[] getAtr() throws CardException {
		assertOpen();
		try {
			return client.getAtr(hChannel);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("channel is closed");
		}
	}

	/**
	 * Returns the handle associated to this card channel.
	 * @return the handle associated to this card channel.
	 */
	long getHandle() {
		return hChannel;
	}
	
	/**
	 * Invalidates this card channel.
	 */
	void invalidate() {
		isClosed = true;
	}

	@Override
	public boolean isClosed() {
		return isClosed;
	}

	@Override
	public boolean isLogicalChannel() {
		return isLogicalChannel;
	}

	@Override
	public byte[] transmit(byte[] command) throws CardException {
		assertOpen();
		try {
			return client.transmit(hChannel, command);
		} catch (IllegalArgumentException e) {
			if (e.getMessage().toLowerCase().startsWith("invalid handle")) {
				throw new IllegalStateException("channel is closed");
			} else {
				throw e;
			}
		}
	}
}
