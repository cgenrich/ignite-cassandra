package com.example.ignite.cassandra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.google.protobuf.ByteString;

public class MainTest {
	private static final Logger LOG = LoggerFactory.getLogger(MainTest.class);
	// private static Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOG);
	private List<Process> servers;
	private static Cluster cluster;
	private static Session session;
	@ClassRule
	public static DockerComposeContainer<?> cassandra = new DockerComposeContainer<>(
			new File("src/test/resources/cassandra.yml")).withExposedService("cassandra", 9042,
					Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)))
	/* .withLogConsumer("cassandra", logConsumer) */;

	@BeforeClass
	public static void clusterClient() {
		cluster = Cluster.builder().withReconnectionPolicy(new ConstantReconnectionPolicy(1000))
				.addContactPoints("127.0.0.1").withPort(9042).build();
		session = cluster.connect("ks");
	}

	@Before
	public void prepare() {
		servers = new ArrayList<>();
	}

	@Test
	public void singleNode() throws Exception {
		test(100, 1);
	}

	@Test
	public void fourNodes() throws Exception {
		test(100, 4);
	}

	@Test
	public void sixNodes() throws Exception {
		test(600, 6);
	}

	@Test
	public void sixNodesWithWait() throws Exception {
		int records = 600, servers = 6;
		for (int i = 0; i < servers; i++) {
			server();
			try (Ignite ignite = client()) {
				int y = 0;
				while (ignite.cluster().forServers().nodes().size() <= i && y++ < 60) {
					try {
						Thread.sleep(1000);
					} catch (Throwable t) {
					}
				}
			}
		}
		test(records);
	}

	private void test(int records, int servers) throws InterruptedException {
		for (int i = 0; i < servers; i++) {
			server();
		}
		test(records);
	}

	private void test(int size) throws InterruptedException {
		try (Ignite ignite = client()) {
			IgniteCache<CompositeKey, CompositeValue> cache = ignite.cache(Main.WRITEBEHIND);
			CountDownLatch latch = new CountDownLatch(size);
			for (int i = 0; i < size; i++) {
				cache.putAsync(new CompositeKey(UUID.randomUUID(), UUID.randomUUID()),
						new CompositeValue(3, 4, "fifth", ByteString.copyFrom("sixth", StandardCharsets.UTF_8),
								ByteString.copyFrom("seventh", StandardCharsets.UTF_8), 8L))
						.chain(f -> {
							latch.countDown();
							return true;
						});
			}
			latch.await(30, TimeUnit.SECONDS);
			assertEquals("callbacks for putAsync not complete", 0, latch.getCount());
			assertEquals(size, cache.size(CachePeekMode.PRIMARY));
			boolean found = false;
			int records=0;
			for (int i = 0; i < 100; i++) {
				try {
					int r=session.execute("select * from tbl").all().size();
					if(r>records) {
						i--;
					}
					assertEquals(size, records=r);
					found = true;
					break;
				} catch (Throwable e) {
					LOG.warn("validation failed wait for write behind, iteration: {} with result {}", i,
							e.getMessage());
					try {
						Thread.sleep(100);
					} catch (Throwable t) {
					}
				}
			}
			assertTrue("did not receive " + size + " records, only: "+records, found);
		}
	}

	@After
	public void stop() throws InterruptedException {
		try (Ignite ignite = client()) {
			ignite.cache(Main.WRITEBEHIND).clear();
			ignite.close();
		} catch (Throwable t) {
			// ignore client errors
		}
		for (Process p : servers) {
			p.destroyForcibly().waitFor(1, TimeUnit.SECONDS);
		}
		session.execute("truncate tbl").all();
	}

	@AfterClass
	public static void close() {
		session.close();
		cluster.close();
	}

	private void server() {
		ProcessBuilder pb = new ProcessBuilder(
				System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java", "-Xmx128m",
				"-cp", System.getProperty("java.class.path"), Main.class.getName());
		pb.inheritIO();
		try {
			servers.add(pb.start());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Ignite client() {
		return Ignition.getOrStart(Main.localCluster(new IgniteConfiguration().setClientMode(true)));
	}
}
