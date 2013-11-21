package org.hive2hive.core.test.process.common.remove;

import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.network.H2HStorageMemory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.common.remove.BaseRemoveProcessStep;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.hive2hive.core.test.process.TestProcessListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RemoveProcessStepTest extends H2HJUnitTest {
	
	private static List<NetworkManager> network;
	private static final int networkSize = 5;
	private String contentKey = "a content key";
	private String data = "some data";

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = RemoveProcessStepTest.class;
		beforeClass();
	}

	@Override
	@Before
	public void beforeMethod() {
		super.beforeMethod();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testRemoveProcessStep() {
		String locationKey = network.get(0).getNodeId();
		H2HTestData testData = new H2HTestData(data);
		// put some data to remove
		network.get(0).getDataManager().putGlobal(locationKey, locationKey, testData).awaitUninterruptibly();

		// initialize the process and the one and only step to test
		Process process = new Process(network.get(0)) {
		};
		BaseRemoveProcessStep putStep = new BaseRemoveProcessStep(locationKey, contentKey, testData, null);
		process.setNextStep(putStep);
		TestProcessListener listener = new TestProcessListener();
		process.addListener(listener);
		process.start();

		// wait for the process to finish
		H2HWaiter waiter = new H2HWaiter(10);
		do {
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
		
		assertNull(network.get(0).getDataManager().getLocal(locationKey, contentKey));
	}
	
	@Test
	public void testRemoveProcessStepFailing() {
		String locationKey = network.get(0).getNodeId();
		H2HTestData testData = new H2HTestData(data);
		// manipulate the node, remove will not work
		network.get(0).getConnection().getPeer().getPeerBean().storage(new FakeGetTestStorage());
		// put some data to remove
		network.get(0).getDataManager().putGlobal(locationKey, locationKey, testData).awaitUninterruptibly();

		// initialize the process and the one and only step to test
		Process process = new Process(network.get(0)) {
		};
		BaseRemoveProcessStep putStep = new BaseRemoveProcessStep(locationKey, contentKey, testData, null);
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

	private class FakeGetTestStorage extends H2HStorageMemory {
		
		public FakeGetTestStorage() {
			super();
		}

		@Override
		public Data get(Number640 key) {
			Data fakeData = null;
			try {
				fakeData = new Data(new H2HTestData(data));
			} catch (IOException e) {
				Assert.fail("Should not happen!");
			}
			return fakeData;
		}
	}
	
}
