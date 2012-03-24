package aQute.aws.sqs;

/**
 * The Message objects contain the meta information of the provider about the
 * message. The message can be used as a receipt for a send operation or as the
 * returned value of a receive operation. In both casee the body is there but
 * only receive messages can change their {@link #setVisibilityTimeout(int)}.
 */

public class Message {
	final String		receipt;
	final String		messageId;
	final MessageQueue	queue;
	final String		content;
	final long			sentTimestamp;
	final long			receiveTimestamp;
	final String		senderId;
	final int			receiveCount;

	Message(MessageQueue queue, String messageId, String content,
			String receipt, long sentTimestamp, long receiveTimestamp,
			int receiveCount, String senderId) {
		this.messageId = messageId;
		this.content = content;
		this.queue = queue;
		this.receipt = receipt;
		this.sentTimestamp = sentTimestamp;
		this.receiveTimestamp = receiveTimestamp;
		this.senderId = senderId;
		this.receiveCount = receiveCount;
	}

	/**
	 * Constructor
	 * 
	 * @param queue
	 * @param messageId
	 * @param body
	 */
	Message(MessageQueue queue, String messageId, String body) {
		this(queue, messageId, body, null, System.currentTimeMillis(), 0, 0,
				null);
	}

	/**
	 * Return the body of this message.
	 * 
	 * @return the body of the message
	 */
	public String getBody() {
		return content;
	}

	/**
	 * Delete this message, this will remove it from the queue.
	 * 
	 * @throws Exception
	 */
	public void delete() throws Exception {
		queue.delete(this);
	}

	/**
	 * Set the visibility timeout of this message.
	 * 
	 * @param seconds number of seconds before this message will become visible
	 *        again.
	 * @throws Exception
	 */
	public void setVisibilityTimeout(int seconds) throws Exception {
		queue.parent.client.action("ChangeMessageVisibility")
				.endpoint(queue.endpoint).arg("ReceiptHandle", receipt)
				.arg("VisibilityTimeout", seconds).check();
	}

	/**
	 * Equals implemented on the message id. That is, two queues are equal of
	 * they have the same message id. The message id is assigned by the queue
	 * provider.
	 */

	public boolean equals(Object o) {
		return o instanceof Message
				&& ((Message) o).messageId.equals(messageId);
	}

	/**
	 * Hashcode implemented on the message id. That is, two queues are equal of
	 * they have the same message id. The message id is assigned by the queue
	 * provider.
	 */
	public int hashCode() {
		return messageId.hashCode();
	}

}
