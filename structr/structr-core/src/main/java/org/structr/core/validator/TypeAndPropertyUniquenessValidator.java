/*
 *  Copyright (C) 2011 Axel Morgner
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

package org.structr.core.validator;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.structr.common.error.ErrorBuffer;
import org.structr.common.SecurityContext;
import org.structr.common.error.EmptyPropertyToken;
import org.structr.common.error.FrameworkException;
import org.structr.common.error.UniqueToken;
import org.structr.core.PropertyValidator;
import org.structr.core.Services;
import org.structr.core.Value;
import org.structr.core.entity.AbstractNode;
import org.structr.core.node.search.Search;
import org.structr.core.node.search.SearchAttribute;
import org.structr.core.node.search.SearchNodeCommand;

/**
 *
 * @author Christian Morgner
 */
public class TypeAndPropertyUniquenessValidator extends PropertyValidator<String> {

	private static final Logger logger = Logger.getLogger(TypeAndPropertyUniquenessValidator.class.getName());

	@Override
	public boolean isValid(String key, Object value, Value<String> parameter, ErrorBuffer errorBuffer) {

		if(value == null || (value != null && value.toString().length() == 0)) {
			errorBuffer.add("TypeAndProperyUniquenessValidator", new EmptyPropertyToken(key));
			return false;
		}

		if(key != null && value != null && parameter != null) {

			if(!(value instanceof String)) {
				return false;
			}

			String type = parameter.get();
			String stringValue = (String)value;
//			User user = new SuperUser();
			AbstractNode topNode = null;
			Boolean includeDeleted = false;
			Boolean publicOnly = false;
			boolean nodeExists = false;

			List<SearchAttribute> attributes = new LinkedList<SearchAttribute>();
			//attributes.add(new TextualSearchAttribute(AbstractNode.Key.type.name(), type, SearchOperator.AND));
			attributes.add(Search.andExactType(type));
			//attributes.add(new TextualSearchAttribute(key, stringValue, SearchOperator.AND));
			attributes.add(Search.andExactProperty(key, stringValue));

			/*
			Semaphore semaphore = null;

			// obtain semaphores and acquire locks
			if(type != null && key != null) {
				semaphore = EntityContext.getSemaphoreForTypeAndProperty(type, key);
				if(semaphore != null) {
					try {	semaphore.acquire(); } catch(InterruptedException iex) { iex.printStackTrace(); }
					logger.log(Level.INFO, "Entering critical section for type {0} key {1} from thread {2}",
					    new Object[] { type, key, Thread.currentThread() } );
				}
			}
			*/

			try {
				List<AbstractNode> resultList = (List<AbstractNode>)Services.command(SecurityContext.getSuperUserInstance(), SearchNodeCommand.class).execute(topNode, includeDeleted, publicOnly, attributes, type, key);
				nodeExists = !resultList.isEmpty();

			} catch(FrameworkException fex ) {
				// handle error
			}

			/*
			if(semaphore != null) {
				semaphore.release();
				logger.log(Level.INFO, "Exiting critical section for type {0} key {1} from thread {2}",
				    new Object[] { type, key, Thread.currentThread() } );
			}
			*/
			
			if(nodeExists) {

				errorBuffer.add("TypeAndProperyUniquenessValidator", new UniqueToken(key, value));
				return false;

			} else {

				return true;
			}

		}

		return false;
	}
}
