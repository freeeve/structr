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

import org.apache.commons.lang.StringUtils;

import org.neo4j.gis.spatial.indexprovider.LayerNodeIndex;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import org.structr.common.error.FrameworkException;
import org.structr.core.EntityContext;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.Location;
import org.structr.core.entity.Person;
import org.structr.core.entity.Principal;
import org.structr.core.node.NodeService.NodeIndex;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.index.lucene.ValueContext;
import org.structr.core.property.PropertyKey;
import org.structr.core.node.search.SearchNodeCommand;

//~--- classes ----------------------------------------------------------------

/**
 * Command for indexing nodes
 *
 * @author Axel Morgner
 */
public class IndexNodeCommand extends NodeServiceCommand {

	private static final Logger logger = Logger.getLogger(IndexNodeCommand.class.getName());

	//~--- fields ---------------------------------------------------------

	private Map<String, Index> indices = new HashMap<String, Index>();

	//~--- methods --------------------------------------------------------

	public void update(final AbstractNode node, PropertyKey propertyKey) throws FrameworkException {

		init();
		indexProperty(node, propertyKey);
	}

	public void update(final AbstractNode node) throws FrameworkException {

		init();
		indexNode(node);
	}

	public void update(final List<AbstractNode> nodes) throws FrameworkException {

		init();
		indexNodes(nodes);
	}
	
	public void add(final AbstractNode node, PropertyKey propertyKey) throws FrameworkException {

		init();
		addProperty(node, propertyKey);
	}

	public void add(final AbstractNode node) throws FrameworkException {

		init();
		addNode(node);
	}

	public void add(final List<AbstractNode> nodes) throws FrameworkException {

		init();
		addNodes(nodes);
	}

	public void remove(final AbstractNode node, PropertyKey propertyKey) throws FrameworkException {

		init();
		removeProperty(node, propertyKey);
	}

	public void remove(final AbstractNode node) throws FrameworkException {

		init();
		removeNode(node);
	}

	public void remove(final List<AbstractNode> nodes) throws FrameworkException {

		init();
		removeNodes(nodes);
	}

	private void init() {

		for (Enum indexName : (NodeIndex[]) arguments.get("indices")) {

			indices.put(indexName.name(), (Index<Node>) arguments.get(indexName.name()));

		}
	}
	
	private void indexNodes(final List<AbstractNode> nodes) {

		for (AbstractNode node : nodes) {

			indexNode(node);

		}
	}

	private void addNodes(final List<AbstractNode> nodes) {

		for (AbstractNode node : nodes) {

			addNode(node);

		}
	}
	
	private void removeNodes(final List<AbstractNode> nodes) {

		for (AbstractNode node : nodes) {

			removeNode(node);

		}
	}
	
	private void indexNode(final AbstractNode node) {

		try {

			String uuid = node.getProperty(AbstractNode.uuid);

			// Don't touch non-structr node
			if (uuid == null) {

				return;

			}

			for (Enum index : (NodeIndex[]) arguments.get("indices")) {

				Set<PropertyKey> properties = EntityContext.getSearchableProperties(node.getClass(), index.name());

				for (PropertyKey key : properties) {

					indexProperty(node, key, index.name());

				}

			}

			Node dbNode = node.getNode();

			if ((dbNode.hasProperty(Location.latitude.dbName())) && (dbNode.hasProperty(Location.longitude.dbName()))) {

				LayerNodeIndex layerIndex = (LayerNodeIndex) indices.get(NodeIndex.layer.name());

				try {

					synchronized (layerIndex) {
					
						layerIndex.add(dbNode, "", "");
					}

					// If an exception is thrown here, the index was deleted
					// and has to be recreated.
				} catch (NotFoundException nfe) {
					
					logger.log(Level.SEVERE, "Could not add node to layer index because the db could not find the node", nfe);
					
				} catch (Exception e) {
					
					logger.log(Level.SEVERE, "Could add node to layer index", e);

//					final Map<String, String> config = new HashMap<String, String>();
//
//					config.put(LayerNodeIndex.LAT_PROPERTY_KEY, Location.Key.latitude.name());
//					config.put(LayerNodeIndex.LON_PROPERTY_KEY, Location.Key.longitude.name());
//					config.put(SpatialIndexProvider.GEOMETRY_TYPE, LayerNodeIndex.POINT_PARAMETER);
//
//					layerIndex = new LayerNodeIndex("layerIndex", graphDb, config);
//					logger.log(Level.WARNING, "Created layer node index due to exception", e);
//
//					indices.put(NodeIndex.layer.name(), layerIndex);
//
//					// try again
//					layerIndex.add(dbNode, "", "");
				}

			}
			
		} catch(Throwable t) {
			
			t.printStackTrace();
			
			logger.log(Level.WARNING, "Unable to index node {0}: {1}", new Object[] { node.getNode().getId(), t.getMessage() } );
			
		}
	}

	private void addNode(final AbstractNode node) {

		try {

			String uuid = node.getProperty(AbstractNode.uuid);

			// Don't touch non-structr node
			if (uuid == null) {

				return;

			}

			for (Enum index : (NodeIndex[]) arguments.get("indices")) {

				Set<PropertyKey> properties = EntityContext.getSearchableProperties(node.getClass(), index.name());

				for (PropertyKey key : properties) {

					addProperty(node, key, index.name());

				}

			}

			Node dbNode = node.getNode();

			if ((dbNode.hasProperty(Location.latitude.dbName())) && (dbNode.hasProperty(Location.longitude.dbName()))) {

				LayerNodeIndex layerIndex = (LayerNodeIndex) indices.get(NodeIndex.layer.name());

				try {

					synchronized (layerIndex) {
					
						layerIndex.add(dbNode, "", "");
					}

					// If an exception is thrown here, the index was deleted
					// and has to be recreated.
				} catch (NotFoundException nfe) {
					
					logger.log(Level.SEVERE, "Could not add node to layer index because the db could not find the node", nfe);
					
				} catch (Exception e) {
					
					logger.log(Level.SEVERE, "Could not add node to layer index", e);

//					final Map<String, String> config = new HashMap<String, String>();
//
//					config.put(LayerNodeIndex.LAT_PROPERTY_KEY, Location.Key.latitude.name());
//					config.put(LayerNodeIndex.LON_PROPERTY_KEY, Location.Key.longitude.name());
//					config.put(SpatialIndexProvider.GEOMETRY_TYPE, LayerNodeIndex.POINT_PARAMETER);
//
//					layerIndex = new LayerNodeIndex("layerIndex", graphDb, config);
//					logger.log(Level.WARNING, "Created layer node index due to exception", e);
//
//					indices.put(NodeIndex.layer.name(), layerIndex);
//
//					// try again
//					layerIndex.add(dbNode, "", "");
				}

			}
			
		} catch(Throwable t) {
			
			t.printStackTrace();
			
			logger.log(Level.WARNING, "Unable to add node {0}: {1}", new Object[] { node.getNode().getId(), t.getMessage() } );
			
		}
	}

	private void removeNode(final AbstractNode node) {

		try {

			String uuid = node.getProperty(AbstractNode.uuid);

			// Don't touch non-structr node
			if (uuid == null) {

				return;

			}

			for (Enum index : (NodeIndex[]) arguments.get("indices")) {

				Set<PropertyKey> properties = EntityContext.getSearchableProperties(node.getClass(), index.name());

				for (PropertyKey key : properties) {

					removeProperty(node, key, index.name());

				}

			}

			Node dbNode = node.getNode();

			if ((dbNode.hasProperty(Location.latitude.dbName())) && (dbNode.hasProperty(Location.longitude.dbName()))) {

				LayerNodeIndex layerIndex = (LayerNodeIndex) indices.get(NodeIndex.layer.name());

				try {

					synchronized (layerIndex) {
					
						layerIndex.remove(dbNode, "", "");
					}

					// If an exception is thrown here, the index was deleted
					// and has to be recreated.
				} catch (NotFoundException nfe) {
					
					logger.log(Level.SEVERE, "Could not remove node from layer index because the db could not find the node", nfe);
					
				} catch (Exception e) {
					
					logger.log(Level.SEVERE, "Could not remove node from layer index", e);

//					final Map<String, String> config = new HashMap<String, String>();
//
//					config.put(LayerNodeIndex.LAT_PROPERTY_KEY, Location.Key.latitude.name());
//					config.put(LayerNodeIndex.LON_PROPERTY_KEY, Location.Key.longitude.name());
//					config.put(SpatialIndexProvider.GEOMETRY_TYPE, LayerNodeIndex.POINT_PARAMETER);
//
//					layerIndex = new LayerNodeIndex("layerIndex", graphDb, config);
//					logger.log(Level.WARNING, "Created layer node index due to exception", e);
//
//					indices.put(NodeIndex.layer.name(), layerIndex);
//
//					// try again
//					layerIndex.add(dbNode, "", "");
				}

			}
			
		} catch(Throwable t) {
			
			t.printStackTrace();
			
			logger.log(Level.WARNING, "Unable to remove node {0}: {1}", new Object[] { node.getNode().getId(), t.getMessage() } );
			
		}
	}
	
	private void indexProperty(final AbstractNode node, final PropertyKey key) {

		for (Enum index : (NodeIndex[]) arguments.get("indices")) {

			Set<PropertyKey> properties = EntityContext.getSearchableProperties(node.getClass(), index.name());

			if ((properties != null) && properties.contains(key)) {

				indexProperty(node, key, index.name());

			}

		}
	}

	private void addProperty(final AbstractNode node, final PropertyKey key) {

		for (Enum index : (NodeIndex[]) arguments.get("indices")) {

			Set<PropertyKey> properties = EntityContext.getSearchableProperties(node.getClass(), index.name());

			if ((properties != null) && properties.contains(key)) {

				addProperty(node, key, index.name());

			}

		}
	}
	
	private void removeProperty(final AbstractNode node, final PropertyKey key) {

		for (Enum index : (NodeIndex[]) arguments.get("indices")) {

			Set<PropertyKey> properties = EntityContext.getSearchableProperties(node.getClass(), index.name());

			if ((properties != null) && properties.contains(key)) {

				removeProperty(node, key, index.name());

			}

		}
	}
	
	
	private void indexProperty(final AbstractNode node, final PropertyKey key, final String indexName) {

		// String type = node.getClass().getSimpleName();
		Node dbNode = node.getNode();
		long id     = node.getId();

		if (key == null) {

			logger.log(Level.SEVERE, "Node {0} has null key", new Object[] { id });

			return;

		}

		boolean emptyKey = StringUtils.isEmpty(key.dbName());

		if (emptyKey) {

			logger.log(Level.SEVERE, "Node {0} has empty, not-null key, removing property", new Object[] { id });
			dbNode.removeProperty(key.dbName());

			return;

		}

		Object value            = node.getProperty(key);    // dbNode.getProperty(key);
		Object valueForIndexing = node.getPropertyForIndexing(key);
		
		if ((value == null && key.databaseConverter(securityContext, null) == null) || (value != null && value instanceof String && StringUtils.isEmpty((String) value))) {
			valueForIndexing = SearchNodeCommand.IMPROBABLE_SEARCH_VALUE;
			value = SearchNodeCommand.IMPROBABLE_SEARCH_VALUE;
		}

		logger.log(Level.FINE, "Indexing value {0} for key {1} on node {2} in {3} index", new Object[] { valueForIndexing, key, id, indexName });
		
		// index.remove(node, key, value);
		removeNodePropertyFromIndex(dbNode, key, indexName);
		logger.log(Level.FINE, "Node {0}: Old value for key {1} removed from {2} index", new Object[] { id, key, indexName });
		addNodePropertyToIndex(dbNode, key, valueForIndexing, indexName);

		if ((node instanceof Principal) && (key.equals(AbstractNode.name) || key.equals(Person.email))) {

			removeNodePropertyFromIndex(dbNode, key, NodeIndex.user.name());
			addNodePropertyToIndex(dbNode, key, valueForIndexing, NodeIndex.user.name());

		}

		if (key.equals(AbstractNode.uuid)) {

			removeNodePropertyFromIndex(dbNode, key, NodeIndex.uuid.name());
			addNodePropertyToIndex(dbNode, key, valueForIndexing, NodeIndex.uuid.name());

		}

		logger.log(Level.FINE, "Node {0}: New value {2} added for key {1}", new Object[] { id, key, value });
	}

	private void addProperty(final AbstractNode node, final PropertyKey key, final String indexName) {

		// String type = node.getClass().getSimpleName();
		Node dbNode = node.getNode();
		long id     = node.getId();

		if (key == null) {

			logger.log(Level.SEVERE, "Node {0} has null key", new Object[] { id });

			return;

		}

		boolean emptyKey = StringUtils.isEmpty(key.dbName());

		if (emptyKey) {

			logger.log(Level.SEVERE, "Node {0} has empty, not-null key, not adding property", new Object[] { id });

			return;

		}

		Object value            = node.getProperty(key);    // dbNode.getProperty(key);
		Object valueForIndexing = node.getPropertyForIndexing(key);
		
		if ((value == null && key.databaseConverter(securityContext, null) == null) || (value != null && value instanceof String && StringUtils.isEmpty((String) value))) {
			valueForIndexing = SearchNodeCommand.IMPROBABLE_SEARCH_VALUE;
			value = SearchNodeCommand.IMPROBABLE_SEARCH_VALUE;
		}

		logger.log(Level.FINE, "Adding value {0} for key {1} on node {2} in {3} index", new Object[] { valueForIndexing, key, id, indexName });
		
		addNodePropertyToIndex(dbNode, key, valueForIndexing, indexName);

		if ((node instanceof Principal) && (key.equals(AbstractNode.name) || key.equals(Person.email))) {

			addNodePropertyToIndex(dbNode, key, valueForIndexing, NodeIndex.user.name());

		}

		if (key.equals(AbstractNode.uuid)) {

			addNodePropertyToIndex(dbNode, key, valueForIndexing, NodeIndex.uuid.name());

		}

		logger.log(Level.FINE, "Node {0}: New value {2} added for key {1}", new Object[] { id, key, value });
	}

	private void removeProperty(final AbstractNode node, final PropertyKey key, final String indexName) {

		// String type = node.getClass().getSimpleName();
		Node dbNode = node.getNode();
		long id     = node.getId();

		if (key == null) {

			logger.log(Level.SEVERE, "Node {0} has null key", new Object[] { id });

			return;

		}

		boolean emptyKey = StringUtils.isEmpty(key.dbName());

		if (emptyKey) {

			logger.log(Level.SEVERE, "Node {0} has empty, not-null key, removing property", new Object[] { id });
			dbNode.removeProperty(key.dbName());

			return;

		}

		Object value            = node.getProperty(key);    // dbNode.getProperty(key);
		Object valueForIndexing = node.getPropertyForIndexing(key);
		
		if ((value == null && key.databaseConverter(securityContext, null) == null) || (value != null && value instanceof String && StringUtils.isEmpty((String) value))) {
			valueForIndexing = SearchNodeCommand.IMPROBABLE_SEARCH_VALUE;
			value = SearchNodeCommand.IMPROBABLE_SEARCH_VALUE;
		}

		logger.log(Level.FINE, "Removing value {0} for key {1} on node {2} in {3} index", new Object[] { valueForIndexing, key, id, indexName });
		
		// index.remove(node, key, value);
		removeNodePropertyFromIndex(dbNode, key, indexName);
		logger.log(Level.FINE, "Node {0}: Old value for key {1} removed from {2} index", new Object[] { id, key, indexName });

		if ((node instanceof Principal) && (key.equals(AbstractNode.name) || key.equals(Person.email))) {

			removeNodePropertyFromIndex(dbNode, key, NodeIndex.user.name());

		}

		if (key.equals(AbstractNode.uuid)) {

			removeNodePropertyFromIndex(dbNode, key, NodeIndex.uuid.name());

		}

		logger.log(Level.FINE, "Node {0}: New value {2} removed for key {1}", new Object[] { id, key, value });
	}
	
	private void removeNodePropertyFromIndex(final Node node, final PropertyKey key, final String indexName) {
		Index<Node> index = indices.get(indexName);
		synchronized(index) {
//			long t0 = System.nanoTime();
			index.remove(node, key.dbName());
//			long t1 = System.nanoTime();
//			System.out.println("removing  " + key.dbName() + " of node " + node + " from index " + indexName + " took " + (t1-t0) + " ns");
		}
	}

	private void addNodePropertyToIndex(final Node node, final PropertyKey key, final Object value, final String indexName) {
		if (value == null) {
			return;
		}
		Index<Node> index = indices.get(indexName);
		synchronized(index) {
//			long t0 = System.nanoTime();
			if (value instanceof Number) {
				index.add(node, key.dbName(), ValueContext.numeric((Number) value));
			} else {
				index.add(node, key.dbName(), value);
			}
//			long t1 = System.nanoTime();
//			System.out.println("adding " + key.dbName() + " of " + node + " to index " + indexName + " took " + (t1-t0) + " ns");
		}
	}
}
