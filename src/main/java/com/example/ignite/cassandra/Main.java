package com.example.ignite.cassandra;

import java.util.Arrays;

import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;

import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.store.cassandra.CassandraCacheStoreFactory;
import org.apache.ignite.cache.store.cassandra.datasource.DataSource;
import org.apache.ignite.cache.store.cassandra.persistence.KeyValuePersistenceSettings;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.springframework.core.io.ClassPathResource;

import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;

public class Main {
	public static final String WRITEBEHIND = "writebehind";

	public static void main(String[] args) throws Throwable {
		DataSource ds = new DataSource();
		ds.setPort(9042);
		ds.setContactPoints("localhost");
		ds.setReconnectionPolicy(new ConstantReconnectionPolicy(1000));
		ds.setAuthProvider(new PlainTextAuthProvider(null, null));
		ds.setReadConsistency("ONE");
		ds.setWriteConsistency("ONE");
		ds.setLoadBalancingPolicy(new TokenAwarePolicy(new RoundRobinPolicy()));
		Ignition.getOrStart(localCluster(new IgniteConfiguration()))
				.getOrCreateCache(new CacheConfiguration<>(WRITEBEHIND).setCacheMode(CacheMode.PARTITIONED)
						.setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(Duration.ONE_HOUR))
						.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC).setBackups(1)
						.setReadThrough(false).setWriteThrough(true).setWriteBehindEnabled(true)
						.setWriteBehindFlushFrequency(100).setCopyOnRead(false).setCacheStoreFactory(
								new CassandraCacheStoreFactory<>().setDataSource(ds).setPersistenceSettings(
										new KeyValuePersistenceSettings(new ClassPathResource("cassandra.xml")))));
	}

	public static IgniteConfiguration localCluster(IgniteConfiguration config) {
		return config.setGridLogger(new Slf4jLogger()).setClientConnectorConfiguration(null)
				.setConnectorConfiguration(null)
				.setCommunicationSpi(new TcpCommunicationSpi().setLocalPort(5000).setLocalPortRange(20))
				.setDiscoverySpi(new TcpDiscoverySpi().setLocalPort(5020).setLocalPortRange(20)
						.setIpFinder(new TcpDiscoveryVmIpFinder().setAddresses(Arrays.asList("localhost:5020..5039"))));
	}
}
