package org.hive2hive.core.test.process.common.put;

import java.security.PublicKey;
import java.util.List;

import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.network.H2HStorageMemory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.put.BasePutProcessStep;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PutProcessStepTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 5;
	private String contentKey = "a content key";
	private String data = "some data";

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = PutProcessStepTest.class;
		beforeClass();
	}

	@Override
	@Before
	public void beforeMethod() {
		super.beforeMethod();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testPutProcessStep() {
		String locationKey = network.get(0).getNodeId();

		// initialize the process and the one and only step to test
		Process process = new Process(network.get(0)) {
		};
		BasePutProcessStep putStep = new BasePutProcessStep(locationKey, contentKey, new H2HTestData(data), null);
		process.setNextStep(putStep);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
	}

	@Test
	public void testPutProcessStepDelayedGetFails() {
		// all nodes will deny the put request
		for (NetworkManager node : network)
			node.getConnection().getPeer().getPeerBean().storage(new DenyingGetTestStorage());

		String locationKey = network.get(0).getNodeId();

		// initialize the process and the one and only step to test
		Process process = new Process(network.get(0)) {
		};
		BasePutProcessStep putStep = new BasePutProcessStep(locationKey, contentKey, new H2HTestData(data), null);
		process.setNextStep(putStep);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.hasFailed());
	}

	@Test
	public void testPutProcessStepAllContactedPeersDenyPut() {
		// all nodes will deny the put request
		for (NetworkManager node : network)
			node.getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());

		String locationKey = network.get(0).getNodeId();

		// initialize the process and the one and only step to test
		Process process = new Process(network.get(0)) {
		};
		BasePutProcessStep putStep = new BasePutProcessStep(locationKey, contentKey, new H2HTestData(data), null);
		process.setNextStep(putStep);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.hasFailed());
	}

	@Test
	public void testPutProcessStepMinorityOfContactedPeersDenyPut() {
		network.get(0).getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());
		network.get(1).getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());

		String locationKey = network.get(0).getNodeId();

		// initialize the process and the one and only step to test
		Process process = new Process(network.get(0)) {
		};
		BasePutProcessStep putStep = new BasePutProcessStep(locationKey, contentKey, new H2HTestData(data), null);
		process.setNextStep(putStep);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
	}

	@Test
	public void testPutProcessStepMajorityOfContactedPeersDenyPut() {
		network.get(1).getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());
		network.get(2).getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());
		network.get(3).getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());
		network.get(4).getConnection().getPeer().getPeerBean().storage(new DenyingPutTestStorage());

		String locationKey = network.get(0).getNodeId();

		// initialize the process and the one and only step to test
		Process process = new Process(network.get(0)) {
		};
		BasePutProcessStep putStep = new BasePutProcessStep(locationKey, contentKey, new H2HTestData(data), null);
		process.setNextStep(putStep);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.hasFailed());
	}

	/**
	 * Not implemented yet.
	 */
	@Test
	public void testPutProcessStepWithOneVersionConflict() {
		network.get(0).getConnection().getPeer().getPeerBean().storage(new VersionConflictTestStorage());

		String locationKey = network.get(0).getNodeId();

		// initialize the process and the one and only step to test
		Process process = new Process(network.get(0)) {
		};
		BasePutProcessStep putStep = new BasePutProcessStep(locationKey, contentKey, new H2HTestData(data), null);
		process.setNextStep(putStep);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.hasFailed());
	}

	@Override
	@After
	public void afterMethod() {
		NetworkTestUtil.shutdownNetwork(network);
		super.afterMethod();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

	private class DenyingPutTestStorage extends H2HStorageMemory {

		public DenyingPutTestStorage() {
			super();
		}

		@Override
		public PutStatusH2H put(Number640 key, Data value, PublicKey publicKey, boolean putIfAbsent,
				boolean domainProtection) {
			// doesn't accept any data
			return PutStatusH2H.FAILED;
		}
	}

	private class DenyingGetTestStorage extends H2HStorageMemory {

		public DenyingGetTestStorage() {
			super();
		}

		@Override
		public Data get(Number640 key) {
			return null;
		}
	}

	private class VersionConflictTestStorage extends H2HStorageMemory {

		public VersionConflictTestStorage() {
			super();
		}

		@Override
		public PutStatusH2H put(Number640 key, Data newData, PublicKey publicKey, boolean putIfAbsent,
				boolean domainProtection) {
			// imitate a version conflict
			return PutStatusH2H.VERSION_CONFLICT;
		}
	}
}