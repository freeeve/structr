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



package org.structr.websocket.message;

import org.structr.common.TreeNode;
import org.structr.core.GraphObject;

//~--- JDK imports ------------------------------------------------------------

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author Christian Morgner
 */
public class WebSocketMessage {

	private String button                  = null;
	private String callback                = null;
	private int chunkSize                  = 512;
	private int code                       = 0;
	private String command                 = null;
	private GraphObject graphObject        = null;
	private String id                      = null;
	private String message                 = null;
	private Map<String, Object> nodeData   = new LinkedHashMap<String, Object>();
	private int page                       = 0;
	private int pageSize                   = 0;
	private String parent                  = null;
	private Map<String, Object> relData    = new LinkedHashMap<String, Object>();
	private Set<String> modifiedProperties = new LinkedHashSet<String>();
	private Set<String> removedProperties = new LinkedHashSet<String>();
	private List<? extends GraphObject> result       = null;
	private TreeNode resultTree            = null;
	private boolean sessionValid           = false;
	private String sortKey                 = null;
	private String sortOrder               = null;
	private String token                   = null;
	private String view                    = null;
	private Set<String> nodesWithChildren  = null;
	private String treeAddress             = null;

	//~--- methods --------------------------------------------------------

	public WebSocketMessage copy() {

		WebSocketMessage newCopy = new WebSocketMessage();

		newCopy.button             = this.button;
		newCopy.callback           = this.callback;
		newCopy.code               = this.code;
		newCopy.command            = this.command;
		newCopy.nodeData           = this.nodeData;
		newCopy.relData            = this.relData;
		newCopy.graphObject        = this.graphObject;
		newCopy.id                 = this.id;
		newCopy.message            = this.message;
		newCopy.modifiedProperties = this.modifiedProperties;
		newCopy.removedProperties  = this.removedProperties;
		newCopy.page               = this.page;
		newCopy.pageSize           = this.pageSize;
		newCopy.parent             = this.parent;
		newCopy.result             = this.result;
		newCopy.resultTree         = this.resultTree;
		newCopy.sessionValid       = this.sessionValid;
		newCopy.sortKey            = this.sortKey;
		newCopy.sortOrder          = this.sortOrder;
		newCopy.token              = this.token;
		newCopy.view               = this.view;
		newCopy.chunkSize          = this.chunkSize;
		newCopy.nodesWithChildren  = this.nodesWithChildren;
		newCopy.treeAddress        = this.treeAddress;

		return newCopy;
	}

	//~--- get methods ----------------------------------------------------

	public String getCommand() {
		return command;
	}

	public String getId() {
		return id;
	}

	public Map<String, Object> getNodeData() {
		return nodeData;
	}

	public Map<String, Object> getRelData() {
		return relData;
	}

	public String getCallback() {
		return callback;
	}

	public String getButton() {
		return button;
	}

	public String getParent() {
		return parent;
	}

	public String getView() {
		return view;
	}

	public List<? extends GraphObject> getResult() {
		return result;
	}

	public String getSortKey() {
		return sortKey;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getPage() {
		return page;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public String getToken() {
		return token;
	}

	public String getMessage() {
		return message;
	}

	public int getCode() {
		return code;
	}

	public Set<String> getModifiedProperties() {
		return modifiedProperties;
	}

	public Set<String> getRemovedProperties() {
		return removedProperties;
	}

	public int getChunkSize() {
		return chunkSize;
	}

	/**
	 * @return the graphObject
	 */
	public GraphObject getGraphObject() {
		return graphObject;
	}

	public TreeNode getResultTree() {
		return resultTree;
	}

	public boolean isSessionValid() {
		return sessionValid;
	}

	public Set<String> getNodesWithChildren() {
		return nodesWithChildren;
	}

	public String getTreeAddress() {
		return treeAddress;
	}

	//~--- set methods ----------------------------------------------------

	public void setCommand(String command) {
		this.command = command;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setNodeData(String key, Object value) {
		nodeData.put(key, value);
	}

	public void setNodeData(Map<String, Object> data) {
		this.nodeData.putAll(data);
	}

	public void setRelData(String key, Object value) {
		relData.put(key, value);
	}

	public void setRelData(Map<String, Object> data) {
		this.relData.putAll(data);
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public void setButton(String button) {
		this.button = button;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public void setView(String view) {
		this.view = view;
	}

	public void setResult(List<? extends GraphObject> result) {
		this.result = result;
	}

	public void setResultTree(TreeNode resultTree) {
		this.resultTree = resultTree;
	}

	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setSessionValid(boolean sessionValid) {
		this.sessionValid = sessionValid;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setModifiedProperties(Set<String> modifiedProperties) {
		this.modifiedProperties = modifiedProperties;
	}
	
	public void setRemovedProperties(Set<String> removedProperties) {
		this.removedProperties = removedProperties;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	/**
	 * @param graphObject the graphObject to set
	 */
	public void setGraphObject(GraphObject graphObject) {
		this.graphObject = graphObject;
	}

	public void setNodesWithChildren(final Set<String> nodesWithChildren) {
		this.nodesWithChildren = nodesWithChildren;
	}
	
	public void setTreeAddress(final String treeAddress) {
		this.treeAddress = treeAddress;
	}
}
