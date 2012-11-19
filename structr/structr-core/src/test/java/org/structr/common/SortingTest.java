/*
*  Copyright (C) 2010-2012 Axel Morgner
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



package org.structr.common;

import org.structr.core.property.PropertyKey;
import org.apache.lucene.search.SortField;

import org.structr.common.error.FrameworkException;
import org.structr.core.Result;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.TestOne;
import org.structr.core.node.search.Search;
import org.structr.core.node.search.SearchAttribute;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.structr.core.Services;
import org.structr.core.node.StructrTransaction;
import org.structr.core.node.TransactionCommand;

//~--- classes ----------------------------------------------------------------

/**
 * Test paging
 *
 * All tests are executed in superuser context
 *
 * @author Axel Morgner
 */
public class SortingTest extends StructrTest {

	private static final Logger logger = Logger.getLogger(SortingTest.class.getName());

	//~--- methods --------------------------------------------------------

	@Override
	public void test00DbAvailable() {

		super.test00DbAvailable();

	}

	public void test01SortByName() {

		try {

			boolean includeDeletedAndHidden = false;
			boolean publicOnly              = false;
			String type                     = TestOne.class.getSimpleName();
			int number                      = 4; // no more than 89 to avoid sort order TestOne-10, TestOne-100 ...
			final List<AbstractNode> nodes  = this.createTestNodes(type, number);
			final int offset                = 10;

			Collections.shuffle(nodes, new Random(System.nanoTime()));

			Services.command(SecurityContext.getSuperUserInstance(), TransactionCommand.class).execute(new StructrTransaction() {

				@Override
				public Object execute() throws FrameworkException {
					
					int i = offset;
					String name;
					
					for (AbstractNode node : nodes) {

						//System.out.println("Node ID: " + node.getNodeId());

						name = "TestOne-" + i;

						i++;

						node.setName(name);

					}

					return null;
				}
			});
			
			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();

			searchAttributes.add(Search.andExactTypeAndSubtypes(type));

			Result result = searchNodeCommand.execute(includeDeletedAndHidden, publicOnly, searchAttributes);

			assertEquals(number, result.size());

			PropertyKey sortKey = AbstractNode.name;
			boolean sortDesc    = false;
			int pageSize        = 10;
			int page            = 1;

			result = searchNodeCommand.execute(includeDeletedAndHidden, publicOnly, searchAttributes, sortKey, sortDesc, pageSize, page);

//                      for (GraphObject obj : result.getResults()) {
//                              
//                              System.out.println(obj.getProperty(AbstractNode.name));
//                              
//                      }
			logger.log(Level.INFO, "Raw result size: {0}, expected: {1}", new Object[] { result.getRawResultCount(), number });
			assertTrue(result.getRawResultCount() == number);
			logger.log(Level.INFO, "Result size: {0}, expected: {1}", new Object[] { result.size(), Math.min(number, pageSize) });
			assertTrue(result.size() == Math.min(number, pageSize));

			for (int j = 0; j < Math.min(result.size(), pageSize); j++) {

				String expectedName = "TestOne-" + (offset + j);
				String gotName     = result.get(j).getProperty(AbstractNode.name);

				System.out.println(expectedName + ", got: " + gotName);
				assertEquals(expectedName, gotName);

			}

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}

	public void test02SortByNameDesc() {

		try {

			boolean includeDeletedAndHidden = false;
			boolean publicOnly              = false;
			String type                     = TestOne.class.getSimpleName();
			int number                      = 43;
			final List<AbstractNode> nodes  = this.createTestNodes(type, number);
			final int offset                = 10;

			Collections.shuffle(nodes, new Random(System.nanoTime()));
			
			Services.command(SecurityContext.getSuperUserInstance(), TransactionCommand.class).execute(new StructrTransaction() {

				@Override
				public Object execute() throws FrameworkException {
			
					int i = offset;
					String name;
					
					for (AbstractNode node : nodes) {

						name = Integer.toString(i);

						i++;

						node.setName(name);

					}
					
					return null;
					
				}
				
			});

			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();

			searchAttributes.add(Search.andType(type));

			Result result = searchNodeCommand.execute(includeDeletedAndHidden, publicOnly, searchAttributes);

			assertEquals(number, result.size());

			PropertyKey sortKey = AbstractNode.name;
			boolean sortDesc    = true;
			int pageSize        = 10;
			int page            = 1;

			result = searchNodeCommand.execute(includeDeletedAndHidden, publicOnly, searchAttributes, sortKey, sortDesc, pageSize, page);

//                      for (GraphObject obj : result.getResults()) {
//                              
//                              System.out.println(obj.getProperty(AbstractNode.name));
//                              
//                      }
			logger.log(Level.INFO, "Raw result size: {0}, expected: {1}", new Object[] { result.getRawResultCount(), number });
			assertTrue(result.getRawResultCount() == number);
			logger.log(Level.INFO, "Result size: {0}, expected: {1}", new Object[] { result.size(), Math.min(number, pageSize) });
			assertTrue(result.size() == Math.min(number, pageSize));

			for (int j = 0; j < Math.min(result.size(), pageSize); j++) {

				int expectedNumber = number + offset - 1 - j;
				String gotName     = result.get(j).getProperty(AbstractNode.name);

				System.out.println(expectedNumber + ", got: " + gotName);
				assertEquals(Integer.toString(expectedNumber), gotName);

			}

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}

	public void test03SortByDate() {

		try {

			boolean includeDeletedAndHidden = false;
			boolean publicOnly              = false;
			String type                     = TestOne.class.getSimpleName();
			int number                      = 97;
			final List<AbstractNode> nodes  = this.createTestNodes(type, number);
			final int offset                = 10;

			Collections.shuffle(nodes, new Random(System.nanoTime()));

			
			Services.command(SecurityContext.getSuperUserInstance(), TransactionCommand.class).execute(new StructrTransaction() {

				@Override
				public Object execute() throws FrameworkException {
			
					int i = offset;
					String name;
					
					for (AbstractNode node : nodes) {

						name = Integer.toString(i);

						i++;

						node.setName("TestOne-" + name);

						node.setProperty(TestOne.aDate, new Date());

						// System.out.println(node.getProperty(AbstractNode.name) + ", " + node.getProperty(TestOne.Key.aDate) + " (set: " + timestamp + ")");

					}
					
					return null;
				}
				
			});

			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();

			searchAttributes.add(Search.andType(type));

			Result result = searchNodeCommand.execute(includeDeletedAndHidden, publicOnly, searchAttributes);

			assertEquals(number, result.size());

			PropertyKey sortKey = TestOne.aDate;
			boolean sortDesc    = false;
			int pageSize        = 10;
			int page            = 1;

			result = searchNodeCommand.execute(includeDeletedAndHidden, publicOnly, searchAttributes, sortKey, sortDesc, pageSize, page, null, SortField.LONG);

//                      for (GraphObject obj : result.getResults()) {
//
//                              System.out.println(obj.getProperty(AbstractNode.name) + ", " + obj.getProperty(TestOne.Key.aDate.name()));
//                      }
			logger.log(Level.INFO, "Raw result size: {0}, expected: {1}", new Object[] { result.getRawResultCount(), number });
			assertTrue(result.getRawResultCount() == number);
			logger.log(Level.INFO, "Result size: {0}, expected: {1}", new Object[] { result.size(), pageSize });
			assertTrue(result.size() == Math.min(number, pageSize));

			for (int j = 0; j < Math.min(result.size(), pageSize); j++) {

				String expectedName = "TestOne-" + (offset + j);
				String gotName     = result.get(j).getProperty(AbstractNode.name);

				System.out.println(expectedName + ", got: " + gotName);
				assertEquals(expectedName, gotName);

			}

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}

	public void test04SortByDateDesc() {

		try {

			boolean includeDeletedAndHidden = false;
			boolean publicOnly              = false;
			String type                     = TestOne.class.getSimpleName();
			int number                      = 131;
			final List<AbstractNode> nodes  = this.createTestNodes(type, number);
			final int offset                = 10;

			Collections.shuffle(nodes, new Random(System.nanoTime()));

			Services.command(SecurityContext.getSuperUserInstance(), TransactionCommand.class).execute(new StructrTransaction() {

				@Override
				public Object execute() throws FrameworkException {
			
					int i = offset;
					String name;
					
					for (AbstractNode node : nodes) {

						name = Integer.toString(i);

						i++;

						node.setName(name);

					}
					
					return null;
				}
				
			});

			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();

			searchAttributes.add(Search.andType(type));

			Result result = searchNodeCommand.execute(includeDeletedAndHidden, publicOnly, searchAttributes);

			assertEquals(number, result.size());

			PropertyKey sortKey = AbstractNode.lastModifiedDate;
			boolean sortDesc    = true;
			int pageSize        = 10;
			int page            = 1;

			result = searchNodeCommand.execute(includeDeletedAndHidden, publicOnly, searchAttributes, sortKey, sortDesc, pageSize, page, null, SortField.LONG);

//                      for (GraphObject obj : result.getResults()) {
//
//                              System.out.println(obj.getProperty(AbstractNode.name) + ", " + obj.getDateProperty(AbstractNode.lastModifiedDate));
//                      }
			logger.log(Level.INFO, "Raw result size: {0}, expected: {1}", new Object[] { result.getRawResultCount(), number });
			assertTrue(result.getRawResultCount() == number);
			logger.log(Level.INFO, "Result size: {0}, expected: {1}", new Object[] { result.size(), pageSize });
			assertTrue(result.size() == Math.min(number, pageSize));

			for (int j = 0; j < pageSize; j++) {

				int expectedNumber = number + offset - 1 - j;
				String gotName     = result.get(j).getProperty(AbstractNode.name);

				System.out.println(expectedNumber + ", got: " + gotName);
				assertEquals(Integer.toString(expectedNumber), gotName);

			}

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}

	public void test05SortByInt() {

		try {

			boolean includeDeletedAndHidden = false;
			boolean publicOnly              = false;
			String type                     = TestOne.class.getSimpleName();
			int number                      = 61;
			final List<AbstractNode> nodes  = this.createTestNodes(type, number);
			final PropertyKey key           = TestOne.anInt;
			final int offset                = 10;

			Collections.shuffle(nodes, new Random(System.nanoTime()));


			Services.command(SecurityContext.getSuperUserInstance(), TransactionCommand.class).execute(new StructrTransaction() {

				@Override
				public Object execute() throws FrameworkException {
			
					int i = offset;

					for (AbstractNode node : nodes) {

						node.setName(Integer.toString(i));

						node.setProperty(key, i);

						i++;

					}
					
					return null;
				}
			});

			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();

			searchAttributes.add(Search.andType(type));

			Result result = searchNodeCommand.execute(includeDeletedAndHidden, publicOnly, searchAttributes);

			assertEquals(number, result.size());

			PropertyKey sortKey = key;
			boolean sortDesc    = false;
			int pageSize        = 5;
			int page            = 1;

			result = searchNodeCommand.execute(includeDeletedAndHidden, publicOnly, searchAttributes, sortKey, sortDesc, pageSize, page, null, SortField.INT);

//                      for (GraphObject obj : result.getResults()) {
//
//                              System.out.println(obj.getProperty(AbstractNode.name) + ": " + obj.getIntProperty(key));
//                      }
			logger.log(Level.INFO, "Raw result size: {0}, expected: {1}", new Object[] { result.getRawResultCount(), number });
			assertTrue(result.getRawResultCount() == number);
			logger.log(Level.INFO, "Result size: {0}, expected: {1}", new Object[] { result.size(), pageSize });
			assertTrue(result.size() == Math.min(number, pageSize));

			for (int j = 0; j < pageSize; j++) {

				int expectedNumber = offset + j;
				int gotNumber      = result.get(j).getIntProperty(key);

				System.out.println("expected: " + expectedNumber + ", got: " + gotNumber);
				assertEquals(expectedNumber, gotNumber);

			}

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}

	public void test06SortByLong() {

		try {

			boolean includeDeletedAndHidden = false;
			boolean publicOnly              = false;
			String type                     = TestOne.class.getSimpleName();
			int number                      = 43;
			final List<AbstractNode> nodes  = this.createTestNodes(type, number);
			final PropertyKey key           = TestOne.aLong;
			final long offset               = 10;

			Collections.shuffle(nodes, new Random(System.nanoTime()));


			Services.command(SecurityContext.getSuperUserInstance(), TransactionCommand.class).execute(new StructrTransaction() {

				@Override
				public Object execute() throws FrameworkException {
			
					long i = offset;
					
					for (AbstractNode node : nodes) {

						node.setName(Long.toString(i));

						node.setProperty(key, i);

						i++;

						System.out.println(node.getProperty(AbstractNode.name) + ": " + node.getLongProperty(key));

					}
					
					return null;
				}
			});

			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();
			searchAttributes.add(Search.andType(type));

			// searchAttributes.add(Search.orExactProperty(TestOne.Key.aLong, "10"));
			Result<AbstractNode> result = searchNodeCommand.execute(includeDeletedAndHidden, publicOnly, searchAttributes);

			assertEquals(number, result.size());

			PropertyKey sortKey = key;
			boolean sortDesc    = false;
			int pageSize        = 20;
			int page            = 1;

			result = searchNodeCommand.execute(includeDeletedAndHidden, publicOnly, searchAttributes, sortKey, sortDesc, pageSize, page, null, SortField.LONG);

			for (AbstractNode obj : result.getResults()) {

				System.out.println(obj.getProperty(AbstractNode.name) + ": " + obj.getLongProperty(key));
			}

			logger.log(Level.INFO, "Raw result size: {0}, expected: {1}", new Object[] { result.getRawResultCount(), number });
			assertTrue(result.getRawResultCount() == number);
			logger.log(Level.INFO, "Result size: {0}, expected: {1}", new Object[] { result.size(), pageSize });

			if (result.size() >= pageSize) {

				assertTrue(result.size() == pageSize);
			}

			for (int j = 0; j < pageSize; j++) {

				long expectedNumber = offset + j;
				long gotNumber      = result.get(j).getLongProperty(key);

				System.out.println("expected: " + expectedNumber + ", got: " + gotNumber);
				assertEquals(expectedNumber, gotNumber);

			}

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}

}
