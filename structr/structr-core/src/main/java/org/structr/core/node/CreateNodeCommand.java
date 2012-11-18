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

import java.lang.Object;
import java.lang.String;
import org.neo4j.graphdb.GraphDatabaseService;

import org.structr.common.RelType;
import org.structr.common.error.FrameworkException;
import org.structr.core.EntityContext;
import org.structr.core.GraphObject;
import org.structr.core.Services;
import org.structr.core.Transformation;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractRelationship;
import org.structr.core.entity.Principal;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.structr.common.Permission;
import org.structr.core.property.PropertyKey;
import org.structr.common.property.PropertyMap;
import org.structr.common.SecurityContext;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author cmorgner
 */
public class CreateNodeCommand<T extends AbstractNode> extends NodeServiceCommand {

	private static final Logger logger = Logger.getLogger(CreateNodeCommand.class.getName());

	public T execute(Collection<NodeAttribute> attributes) throws FrameworkException {
		
		PropertyMap properties = new PropertyMap();
		for (NodeAttribute attribute : attributes) {
			
			properties.put(attribute.getKey(), attribute.getValue());
		}
		
		return execute(properties);
		
	}
	
	public T execute(NodeAttribute... attributes) throws FrameworkException {
		
		PropertyMap properties = new PropertyMap();
		for (NodeAttribute attribute : attributes) {
			
			properties.put(attribute.getKey(), attribute.getValue());
		}
		
		return execute(properties);
	}
	
	public T execute(PropertyMap attributes) throws FrameworkException {

		GraphDatabaseService graphDb = (GraphDatabaseService) arguments.get("graphDb");
		Principal user               = securityContext.getUser();
		T node	                     = null;

		if (graphDb != null) {

			CreateRelationshipCommand createRel = Services.command(securityContext, CreateRelationshipCommand.class);
			String genericNodeType              = EntityContext.getGenericFactory().createGenericNode().getClass().getSimpleName();
			Date now                            = new Date();

			// Determine node type
			PropertyMap properties = new PropertyMap(attributes);
			Object typeObject      = properties.get(AbstractNode.type);
			String nodeType        = (typeObject != null) ? typeObject.toString() : genericNodeType;

			NodeFactory<T> nodeFactory = new NodeFactory<T>(SecurityContext.getSuperUserInstance());
			
			// Create node with type
			node = nodeFactory.createNodeWithType(graphDb.createNode(), nodeType);
			if (node != null) {
				
				if ((user != null) && user instanceof AbstractNode) {

					AbstractNode owner = (AbstractNode) user;
					node.setProperty(AbstractNode.ownerId, owner.getUuid(), false);
	//
					AbstractRelationship securityRel = createRel.execute(owner, node, RelType.SECURITY, true);    // avoid duplicates

					securityRel.setAllowed(Permission.values());
					logger.log(Level.FINEST, "All permissions given to user {0}", user.getProperty(AbstractNode.name));
					node.unlockReadOnlyPropertiesOnce();
					node.setProperty(AbstractNode.createdBy, user.getProperty(AbstractNode.uuid), false);

				}

				node.unlockReadOnlyPropertiesOnce();
				node.setProperty(AbstractNode.createdDate, now, false);
				node.setProperty(AbstractNode.lastModifiedDate, now, false);
				logger.log(Level.FINE, "Node {0} created", node.getId());

				// set type first!!
				node.setProperty(AbstractNode.type, nodeType, false);
				properties.remove(AbstractNode.type);

				for (Entry<PropertyKey, Object> attr : properties.entrySet()) {

					Object value = attr.getValue();
					
					// FIXME: synthetic Property generation
					node.setProperty(attr.getKey(), value, false);

				}

				properties.clear();
			}

		}

		if (node != null) {

			// notify node of its creation
			node.onNodeCreation();

			// iterate post creation transformations
			for (Transformation<GraphObject> transformation : EntityContext.getEntityCreationTransformations(node.getClass())) {

				transformation.apply(securityContext, node);

			}

			// allow modification listener to examine creation
//                      EntityContext.getGlobalModificationListener().graphObjectCreated(securityContext, node);
		}

		return node;
	}
}
