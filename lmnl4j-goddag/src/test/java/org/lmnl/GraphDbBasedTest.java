package org.lmnl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class GraphDbBasedTest {
	protected static GraphDatabaseService db;
	protected Transaction transaction;

	@BeforeClass
	public static void initGraphDb() {
		db = new EmbeddedGraphDatabase("/Users/gregor/Documents/Dissertation/db");
	}

	@AfterClass
	public static void shutdownGraphDb() {
		if (db != null) {
			db.shutdown();
			db = null;
		}
	}

	@Before
	public void startTransaction() {
		if (db != null) {
			transaction = db.beginTx();
		}
	}

	@After
	public void endTransaction() {
		if (transaction != null) {
			transaction.finish();
			transaction = null;
		}
	}
}
