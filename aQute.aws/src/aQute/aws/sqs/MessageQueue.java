package aQute.aws.sqs;

import java.util.*;

import org.osgi.service.log.*;
import org.w3c.dom.*;

import aQute.aws.*;

/**
 * A Simple Queue service Message Queue.
 * 
 * Provides access to the SQ service queues.
 */
public class MessageQueue {
	final String	name;
	final SQS		parent;
	final String	endpoint;

	MessageQueue(SQS parent, String endpoint) {
		this.parent = parent;
		this.endpoint = endpoint;
		String s = endpoint.toString();
		int l = s.lastIndexOf('/');
		if (l >= 0)
			name = s.substring(l + 1);
		else
			name = s;
	}

	/**
	 * Answer the name of this queue.
	 * 
	 * @return the name of this queue
	 */
	public String getName() {
		return name;
	}

	/**
	 * Receive a list of messages, the size can be 0.
	 * 
	 * @param maximumNumberOfMessages
	 * @param visibilityTimeout
	 * @return
	 * @throws Exception
	 */
	public List<Message> receive(int maximumNumberOfMessages,
			int visibilityTimeout) throws Exception {
		Request request = parent.client.action("ReceiveMessage")
				.endpoint(endpoint).arg("AttributeName.1", "All");
		if (maximumNumberOfMessages >= 2)
			request.arg("MaximumNumberOfMessages", maximumNumberOfMessages);
		if (visibilityTimeout > 0)
			request.arg("VisibilityTimeout", visibilityTimeout);

		request.check();

		List<Message> result = new ArrayList<Message>();
		for (Node node : request
				.nodes("ReceiveMessageResponse/ReceiveMessageResult/Message")) {

			String receiptHandle = request.string(node, "ReceiptHandle");
			String messageId = request.string(node, "MessageId");
			String md5OfBody = request.string(node, "MD5OfBody");
			String body = request.string(node, "Body");
			if (request.checkmd5(md5OfBody, body)) {
				long sentTimestamp = Long.parseLong(request.string(node,
						"Attribute[Name='SentTimestamp']/Value"));
				int receiveCount = Integer.parseInt(request.string(node,
						"Attribute[Name='ApproximateReceiveCount']/Value"));
				long receiveTimestamp = Long
						.parseLong(request
								.string(node,
										"Attribute[Name='ApproximateFirstReceiveTimestamp']/Value"));
				String senderId = request.string(node,
						"Attribute[Name='SenderId']/Value");
				Message msg = new Message(this, messageId, body, receiptHandle,
						sentTimestamp, receiveTimestamp, receiveCount, senderId);
				result.add(msg);
			}
			else
				parent.log.log(LogService.LOG_WARNING,
						"Received false md5, ignored " + receiptHandle + " "
								+ messageId);
		}
		return result;
	}

	/**
	 * Send a message to the queue.
	 * 
	 * @param delay Number of seconds before it becomes active in the queue
	 * @param messages Messages, will be translated to string with the
	 *        toString() method
	 * @return The message objects with the message ids.
	 * @throws Exception
	 */
	public List<Message> send(int delay, Object... messages) throws Exception {
		Request request = parent.client.action("SendMessageBatch").endpoint(endpoint);

		for (int i = 0; i < messages.length; i++) {
			messages[i] = messages[i].toString();
			request.arg("SendMessageBatchRequestEntry." + (i + 1) + ".Id", i);
			request.arg("SendMessageBatchRequestEntry." + (i + 1)
					+ ".MessageBody", messages[i]);
			if (delay != 0)
				request.arg("SendMessageBatchRequestEntry." + i + 1
						+ ".DelaySeconds", delay);
		}

		int i = 0;
		List<Message> result = new ArrayList<Message>();

		for (Node node : request
				.nodes("SendMessageBatchResponse/SendMessageBatchResult/SendMessageBatchResultEntry")) {
			int id = Integer.parseInt(request.string(node, "Id"));
			String messageId = request.string(node, "MessageId");
			String md5OfBody = request.string(node, "MD5OfMessageBody");
			if (request.checkmd5(md5OfBody, (String) messages[i])) {
				result.add(new Message(this, messageId, (String) messages[id]));
			}
			else
				parent.log.log(LogService.LOG_WARNING,
						"Invalid md5 after sending message " + messageId);

			i++;
		}
		return result;
	}

	/**
	 * Delete the messages from the queue.
	 * 
	 * @param messages the list of messages
	 * @throws Exception
	 */
	public void delete(Message... messages) throws Exception {
		Request request = parent.client.action("DeleteMessageBatch").endpoint(endpoint);
		for (int i = 0; i < messages.length; i++) {
			request.arg("DeleteMessageBatchRequestEntry." + (i + 1) + ".Id", i);
			request.arg("DeleteMessageBatchRequestEntry." + (i + 1)
					+ ".ReceiptHandle", ((Message) messages[i]).receipt);
		}
		request.check();
	}

	/**
	 * Send a single message
	 * 
	 * @param message
	 * @return the message with the message id
	 * @throws Exception
	 */
	public Message send(Object message) throws Exception {
		List<Message> send = send(0, new Object[] {message});
		return send.get(0);
	}

	/**
	 * Equals implemented on the queue endpoint. That is, two queues are equal
	 * of they have the same endpoint.
	 */
	public boolean equals(Object o) {
		return o instanceof MessageQueue
				&& ((MessageQueue) o).endpoint.equals(endpoint);
	}

	/**
	 * Hashcode implemented on the queue endpoint.That is, two queues are equal
	 * of they have the same endpoint.
	 */
	public int hashCode() {
		return endpoint.hashCode();
	}

	/**
	 * Receive a message, will return null of no messages available. This queue
	 * will not wait for the messages.
	 * 
	 * @return a message or null
	 * @throws Exception
	 */
	public Message receive() throws Exception {
		List<Message> list = receive(1, 0);
		if (list.isEmpty())
			return null;
		else
			return list.get(0);
	}

}
