package aQute.impl.hazelcast;

import java.util.*;
import java.util.concurrent.*;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import aQute.impl.hazelcast.HazelcastImpl.Cfg;

import com.hazelcast.config.*;
import com.hazelcast.core.*;
import com.hazelcast.logging.*;
import com.hazelcast.partition.*;

@Component(designateFactory = Cfg.class, immediate = true)
public class HazelcastImpl implements HazelcastInstance {
	HazelcastInstance	instance;

	interface Cfg {
		boolean multicast();

		String name();
	}

	Cfg	config;

	@Activate
	void activate(Map<String,Object> props) {
		config = Configurable.createConfigurable(Cfg.class, props);
		Config c = new XmlConfigBuilder().build();
		if (config.name() != null)
			c.setInstanceName(config.name());

		c.setNetworkConfig(//
		new NetworkConfig().//
				setJoin(new Join().setMulticastConfig(new MulticastConfig().//
						setEnabled(config.multicast()))));

		instance = Hazelcast.newHazelcastInstance(c);
	}

	@Deactivate
	void deactivate() {
		instance.getLifecycleService().kill();
	}

	/**
	 * @param arg0
	 * @see com.hazelcast.core.HazelcastInstance#addInstanceListener(com.hazelcast.core.InstanceListener)
	 */
	public void addInstanceListener(InstanceListener arg0) {
		instance.addInstanceListener(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getAtomicNumber(java.lang.String)
	 */
	public AtomicNumber getAtomicNumber(String arg0) {
		return instance.getAtomicNumber(arg0);
	}

	/**
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getClientService()
	 */
	public ClientService getClientService() {
		return instance.getClientService();
	}

	/**
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getCluster()
	 */
	public Cluster getCluster() {
		return instance.getCluster();
	}

	/**
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getConfig()
	 */
	public Config getConfig() {
		return instance.getConfig();
	}

	/**
	 * @param arg0
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getCountDownLatch(java.lang.String)
	 */
	public ICountDownLatch getCountDownLatch(String arg0) {
		return instance.getCountDownLatch(arg0);
	}

	/**
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getExecutorService()
	 */
	public ExecutorService getExecutorService() {
		return instance.getExecutorService();
	}

	/**
	 * @param arg0
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getExecutorService(java.lang.String)
	 */
	public ExecutorService getExecutorService(String arg0) {
		return instance.getExecutorService(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getIdGenerator(java.lang.String)
	 */
	public IdGenerator getIdGenerator(String arg0) {
		return instance.getIdGenerator(arg0);
	}

	/**
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getInstances()
	 */
	public Collection<Instance> getInstances() {
		return instance.getInstances();
	}

	/**
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getLifecycleService()
	 */
	public LifecycleService getLifecycleService() {
		return instance.getLifecycleService();
	}

	/**
	 * @param arg0
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getList(java.lang.String)
	 */
	public <E> IList<E> getList(String arg0) {
		return instance.getList(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getLock(java.lang.Object)
	 */
	public ILock getLock(Object arg0) {
		return instance.getLock(arg0);
	}

	/**
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getLoggingService()
	 */
	public LoggingService getLoggingService() {
		return instance.getLoggingService();
	}

	/**
	 * @param arg0
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getMap(java.lang.String)
	 */
	public <K, V> IMap<K,V> getMap(String arg0) {
		return instance.getMap(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getMultiMap(java.lang.String)
	 */
	public <K, V> MultiMap<K,V> getMultiMap(String arg0) {
		return instance.getMultiMap(arg0);
	}

	/**
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getName()
	 */
	public String getName() {
		return instance.getName();
	}

	/**
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getPartitionService()
	 */
	public PartitionService getPartitionService() {
		return instance.getPartitionService();
	}

	/**
	 * @param arg0
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getQueue(java.lang.String)
	 */
	public <E> IQueue<E> getQueue(String arg0) {
		return instance.getQueue(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getSemaphore(java.lang.String)
	 */
	public ISemaphore getSemaphore(String arg0) {
		return instance.getSemaphore(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getSet(java.lang.String)
	 */
	public <E> ISet<E> getSet(String arg0) {
		return instance.getSet(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getTopic(java.lang.String)
	 */
	public <E> ITopic<E> getTopic(String arg0) {
		return instance.getTopic(arg0);
	}

	/**
	 * @return
	 * @see com.hazelcast.core.HazelcastInstance#getTransaction()
	 */
	public Transaction getTransaction() {
		return instance.getTransaction();
	}

	/**
	 * @param arg0
	 * @see com.hazelcast.core.HazelcastInstance#removeInstanceListener(com.hazelcast.core.InstanceListener)
	 */
	public void removeInstanceListener(InstanceListener arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @see com.hazelcast.core.HazelcastInstance#restart()
	 */
	public void restart() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @see com.hazelcast.core.HazelcastInstance#shutdown()
	 */
	public void shutdown() {
		throw new UnsupportedOperationException();
	}

}
