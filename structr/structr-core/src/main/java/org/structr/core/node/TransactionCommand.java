/*
 *  Copyright (C) 2010-2012 Axel Morgner, structr <structr@structr.org>
 *
 *  This file is part of structr <http://structr.org>.
 *
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */



package org.structr.core.node;


import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.kernel.DeadlockDetectedException;
import org.neo4j.kernel.TopLevelTransaction;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.EntityContext;
import org.structr.core.GraphObject;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author cmorgner
 */
public class TransactionCommand extends NodeServiceCommand {

	private static final Logger logger                 = Logger.getLogger(TransactionCommand.class.getName());
	private static final AtomicLong transactionCounter = new AtomicLong(0);


	//~--- methods --------------------------------------------------------

	@Override
	public Object execute(Object... parameters) throws FrameworkException {

		GraphDatabaseService graphDb             = (GraphDatabaseService) arguments.get("graphDb");
		boolean topLevelTransaction              = false;
		FrameworkException exception             = null;
		Object ret                               = null;

		if ((parameters.length > 0) && (parameters[0] instanceof StructrTransaction)) {

			StructrTransaction transaction = (StructrTransaction) parameters[0];
			Transaction tx                 = graphDb.beginTx();
			topLevelTransaction            = tx instanceof TopLevelTransaction;
			
			try {

				ret = transaction.execute();

				tx.success();
				logger.log(Level.FINEST, "Transaction successfull");

			} catch (FrameworkException frameworkException) {

				tx.failure();
				logger.log(Level.WARNING, "Transaction failure", frameworkException);

				// store exception for later use
				exception = frameworkException;

			} catch(DeadlockDetectedException ddex) {

				tx.failure();

				logger.log(Level.SEVERE, "Neo4j detected a deadlock!", ddex.getMessage());

				/*
					* Maybe the transaction can be restarted here
					*/

			} finally {

				long transactionKey = nextLong();
				EntityContext.setSecurityContext(securityContext);
				EntityContext.setTransactionKey(transactionKey);

				try {
					tx.finish();
				} catch (Throwable t) {

					// transaction failed, look for "real" cause..
					exception = EntityContext.getFrameworkException(transactionKey);
				}
			}

		} else if ((parameters.length > 0) && (parameters[0] instanceof BatchTransaction)) {

			BatchTransaction transaction = (BatchTransaction) parameters[0];
			Transaction tx               = graphDb.beginTx();
			topLevelTransaction          = tx instanceof TopLevelTransaction;
			

			try {

				ret = transaction.execute(tx);

				tx.success();
				logger.log(Level.FINEST, "Transaction successfull");


			} catch (FrameworkException frameworkException) {

				tx.failure();
				logger.log(Level.WARNING, "Transaction failure", frameworkException);

				// store exception for later use
				exception = frameworkException;

			} catch(DeadlockDetectedException ddex) {

				tx.failure();

				logger.log(Level.SEVERE, "Neo4j detected a deadlock!", ddex.getMessage());

				/*
					* Maybe the transaction can be restarted here
					*/

			} finally {

				long transactionKey = nextLong();
				EntityContext.setSecurityContext(securityContext);
				EntityContext.setTransactionKey(transactionKey);

				try {
					tx.finish();
				} catch (Throwable t) {

					// transaction failed, look for "real" cause..
					exception = EntityContext.getFrameworkException(transactionKey);
				}
			}
		}

		if(exception != null) {
			throw exception;
		}

		
		if(topLevelTransaction) {

			Transaction postProcessingTransaction = graphDb.beginTx();

			try {

				afterCreation(securityContext, EntityContext.getCreatedNodes());
				afterCreation(securityContext, EntityContext.getCreatedRelationships());

				afterModification(securityContext, EntityContext.getModifiedNodes());
				afterModification(securityContext, EntityContext.getModifiedRelationships());

				afterDeletion(securityContext, EntityContext.getDeletedNodes());
				afterDeletion(securityContext, EntityContext.getDeletedRelationships());

				// clear aggregated transaction data
				EntityContext.clearTransactionData();

				postProcessingTransaction.success();


			} catch (Throwable t) {

				postProcessingTransaction.failure();

			} finally {

				// enable post-processing of the secondary transaction
				long transactionKey = nextLong();
				EntityContext.setSecurityContext(securityContext);
				EntityContext.setTransactionKey(transactionKey);

				try {
					postProcessingTransaction.finish();
				} catch (Throwable t) {

					// transaction failed, look for "real" cause..
					//t.printStackTrace();
					logger.log(Level.FINE, "Transaction failure", t);
				}

				// clear transaction data
				EntityContext.clearTransactionData();

			}
		}
		
		return ret;
	}
	
	private void afterCreation(SecurityContext securityContext, Set<? extends GraphObject> data) {
		
		if(data != null && !data.isEmpty()) {
			
			for(GraphObject obj : data) {
				obj.afterCreation(securityContext);
			}
		}
		
	}

	private void afterModification(SecurityContext securityContext, Set<? extends GraphObject> data) {
		
		if(data != null && !data.isEmpty()) {
			
			for(GraphObject obj : data) {
				obj.afterModification(securityContext);
			}
		}
	}

	private void afterDeletion(SecurityContext securityContext, Set<? extends GraphObject> data) {
		
		if(data != null && !data.isEmpty()) {
			
			for(GraphObject obj : data) {
				obj.afterDeletion(securityContext);
			}
		}
	}

	private long nextLong() {
		return transactionCounter.incrementAndGet();
	}
}
