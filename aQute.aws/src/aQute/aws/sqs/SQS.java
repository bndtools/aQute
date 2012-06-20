package aQute.aws.sqs;

import java.net.*;
import java.util.*;

import org.osgi.service.log.*;
import org.w3c.dom.*;

import aQute.aws.*;

/**
 * A service interface based on Amazon's Simple Queue Service (SQS) {@see
 * http://aws.amazon.com/sqs/}. The SQ Service provides a fluent interface to
 * the queuing service. It uses {@link MessageQueue} and {@link Message} objects
 * to model the service.
 */
public class SQS {
	public final static String	version	= "2011-10-01";
	URL							region;
	LogService					log;
	final Protocol				client;

	public SQS(Protocol protocol) {
		this.client = protocol;
	}

	public SQS setEndpoint(URL endpoint) {
		this.region = endpoint;
		return this;
	}

	/**
	 * A fluid interface to create a named queue. This returns a
	 * {@link CreateQueueRequest} object that contains the options for the
	 * queue. Finally calling {@link CreateQueueRequest#get()} will create the
	 * queue.
	 * 
	 * @param name
	 *            The name of the queue, maximum 80 characters; alphanumeric
	 *            characters, hyphens (-), and underscores (_) are allowed.
	 * @return A fluid interface for the remaining arguments
	 * @throws Exception
	 */
	public CreateQueueRequest createQueue(String queue) throws Exception {
		assert queue.matches("[0-9a-zA-Z-_]{80}");
		return new CreateQueueRequest(this, queue);
	}

	/**
	 * Delete the queue, if the queue does not exist then no error is generated
	 */

	/**
	 * Delete the queue with the given name if it exists.
	 * 
	 * @param name
	 *            The name of the queue, maximum 80 characters; alphanumeric
	 *            characters, hyphens (-), and underscores (_) are allowed.
	 * @throws Exception
	 */
	public void deleteQueue(MessageQueue queue) throws Exception {
		MessageQueue mi = (MessageQueue) queue;
		client.action("DeleteQueue").endpoint(mi.endpoint)//
				.check("AWS.SimpleQueueService.NonExistentQueue");
	}

	/**
	 * List the queues with the given prefix. If the prefix is null, all queues
	 * are returned.
	 * 
	 * @param prefix
	 *            the prefix or null
	 * @return a list of message queues
	 * @throws Exception
	 */
	public List<MessageQueue> listQueues(String prefix) throws Exception {
		Request request = client.action("ListQueues").arg("QueueNamePrefix", prefix);
		List<MessageQueue> result = new ArrayList<MessageQueue>();
		for (Node node : request.nodes("ListQueuesResponse/ListQueuesResult/QueueUrl")) {
			String url = node.getTextContent().trim();
			result.add(new MessageQueue(this, url));
		}
		return result;
	}

	/**
	 * Return the queue object for the given name
	 * 
	 * @param name
	 *            The name of the queue, maximum 80 characters; alphanumeric
	 *            characters, hyphens (-), and underscores (_) are allowed.
	 * @return the queue object or null of no such queue
	 * @throws Exception
	 */
	public MessageQueue getQueue(String name) throws Exception {
		Request request = client.action("GetQueueUrl").arg("QueueName", name)
				.check("AWS.SimpleQueueService.NonExistentQueue");
		if (request.getError() != null)
			return null; // no such queue (otherwise check would have thrown up)

		String url = request.string("GetQueueUrlResponse/GetQueueUrlResult/QueueUrl");
		return new MessageQueue(this, url);
	}

	static public class CreateQueueRequest {
		final SQS		sqs;
		final Request	request;
		int				attribute	= 1;
		final String	name;

		CreateQueueRequest(SQS sqs, String queueName) throws Exception {
			request = sqs.client.action("CreateQueue").arg("QueueName", queueName);
			this.sqs = sqs;
			this.name = queueName;
		}

		/**
		 * Optional seconds that the messages will be visible. The default for
		 * this attribute is 30.
		 * 
		 * @param seconds
		 *            From 0 to 43200 (12 hours).
		 * @return self
		 */
		public CreateQueueRequest visibilityTimeOut(int seconds) {
			request.arg("VisibilityTimeout", seconds);
			return this;
		}

		/**
		 * Maximum message size. The default for this attribute is 65536 (64
		 * KiB)
		 * 
		 * @param size
		 *            An integer from 1024 bytes (1 KiB) up to 65536 bytes (64
		 *            KiB).
		 * @return self
		 */
		public CreateQueueRequest maximumMessageSize(int size) {
			request.arg("MaximumMessageSize", size);
			return this;
		}

		/**
		 * Maximum period the queue will maintain a message without delivery.
		 * The default for this attribute is 345600 (4 days).
		 * 
		 * @param seconds
		 *            Integer representing seconds, from 60 (1 minute) to
		 *            1209600 (14 days).
		 * @return self
		 */
		public CreateQueueRequest retentionPeriod(int seconds) {
			request.arg("MessageRetentionPeriod", seconds);
			return this;
		}

		/**
		 * Number of seconds before the messages becomes visible in the queue.
		 * The default for this attribute is 0 (zero).
		 * 
		 * @param seconds
		 *            An integer from 0 to 900 (15 minutes).
		 * @return self
		 */
		public CreateQueueRequest delay(int seconds) {
			request.arg("DelaySeconds", seconds);
			return this;
		}

		/**
		 * Create the message queue. If the message queue already exists then
		 * the existing queue is returned.
		 * 
		 * @return the new message queue
		 * @throws Exception
		 */
		public MessageQueue get() throws Exception {
			String url = request.string("CreateQueueResponse/CreateQueueResult/QueueUrl");

			assert url.matches("http.?://.*/" + name);
			return new MessageQueue(sqs, url);
		}

	}
}
