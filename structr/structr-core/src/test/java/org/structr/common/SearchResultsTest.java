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
import org.structr.common.property.PropertyMap;
import org.structr.common.error.FrameworkException;
import org.structr.core.Result;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractRelationship;
import org.structr.core.entity.TestOne;
import org.structr.core.node.search.FilterSearchAttribute;
import org.structr.core.node.search.Search;
import org.structr.core.node.search.SearchAttribute;
import org.structr.core.node.search.SearchOperator;
import org.structr.core.node.search.TextualSearchAttribute;

//~--- JDK imports ------------------------------------------------------------

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.structr.common.property.StringProperty;
import org.structr.core.Services;
import org.structr.core.node.StructrTransaction;
import org.structr.core.node.TransactionCommand;

//~--- classes ----------------------------------------------------------------

/**
 * Test basic search for nodes
 *
 * All tests are executed in superuser context
 *
 * @author Axel Morgner
 */
public class SearchResultsTest extends StructrTest {

	private static final Logger logger = Logger.getLogger(SearchResultsTest.class.getName());

	//~--- methods --------------------------------------------------------

	@Override
	public void test00DbAvailable() {

		super.test00DbAvailable();

	}

	public void test01SearchSingleNodeByName() {

		try {

			PropertyMap props = new PropertyMap();
			final PropertyKey key   = AbstractNode.name;
			final String name1       = "89w3hklsdfghsdkljth";

			props.put(key, name1);

			final AbstractNode node                = createTestNode(TestOne.class.getSimpleName(), props);
			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();
			boolean includeDeletedAndHidden        = true;
			boolean publicOnly                     = false;

			searchAttributes.add(new TextualSearchAttribute(key, name1, SearchOperator.AND));

			Result result = searchNodeCommand.execute(includeDeletedAndHidden, publicOnly, searchAttributes);

			assertTrue(result.size() == 1);
			assertTrue(result.get(0).equals(node));

			// Change name attribute and search again
			final String name2 = "klppptzoehigösoiutzüw0e9hg";
			
			Services.command(SecurityContext.getSuperUserInstance(), TransactionCommand.class).execute(new StructrTransaction() {

				@Override
				public Object execute() throws FrameworkException {
					
					node.setProperty(key, name2);
					
					return null;
				}
				
			});
			searchAttributes.clear();
			searchAttributes.add(new TextualSearchAttribute(key, name2, SearchOperator.AND));

			result = searchNodeCommand.execute(includeDeletedAndHidden, publicOnly, searchAttributes);

			assertTrue(result.size() == 1);
			assertTrue(result.get(0).equals(node));

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}

	public void test02SearchSingleNodeByDate() {

		try {

			PropertyMap props = new PropertyMap();
			PropertyKey key   = TestOne.aDate;
			Date date         = new Date();
			String type       = TestOne.class.getSimpleName();

			props.put(key, date);

			AbstractNode node                      = createTestNode(type, props);
			boolean includeDeletedAndHidden        = true;
			boolean publicOnly                     = false;
			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();

			searchAttributes.add(new TextualSearchAttribute(AbstractNode.type, type, SearchOperator.AND));
			searchAttributes.add(new FilterSearchAttribute(key, date, SearchOperator.AND));

			Result result = searchNodeCommand.execute(includeDeletedAndHidden, publicOnly, searchAttributes);

			assertEquals(1, result.size());
			assertTrue(result.get(0).equals(node));

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}

	public void test03SearchRelationship() {

		try {

			final AbstractRelationship rel = ((List<AbstractRelationship>) createTestRelationships(RelType.UNDEFINED, 1)).get(0);
			final PropertyKey key1         = new StringProperty("jghsdkhgshdhgsdjkfgh");
			final String val1              = "54354354546806849870";

			
			Services.command(SecurityContext.getSuperUserInstance(), TransactionCommand.class).execute(new StructrTransaction() {

				@Override
				public Object execute() throws FrameworkException {

					rel.setProperty(key1, val1);
					
					return null;
					
				}
			});
					
			assertTrue(rel.getProperty(key1).equals(val1));

			List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();

			searchAttributes.add(Search.andExactProperty(key1, val1));

			List<AbstractRelationship> result = (List<AbstractRelationship>) searchRelationshipCommand.execute(searchAttributes);

			assertTrue(result.size() == 1);
			assertTrue(result.get(0).equals(rel));
			searchAttributes.clear();

			String val2 = "ölllldjöoa8w4rasf";

			rel.setProperty(key1, val2);
			searchAttributes.add(Search.andExactProperty(key1, val2));
			assertTrue(result.size() == 1);
			assertTrue(result.get(0).equals(rel));

		} catch (FrameworkException ex) {

			logger.log(Level.SEVERE, ex.toString());
			fail("Unexpected exception");

		}

	}

}
