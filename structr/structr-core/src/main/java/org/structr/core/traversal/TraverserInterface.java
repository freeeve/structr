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

package org.structr.core.traversal;

import java.util.Comparator;
import java.util.List;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.structr.common.SecurityContext;
import org.structr.core.Predicate;
import org.structr.core.entity.AbstractNode;
import org.structr.core.notion.Notion;

/**
 *
 * @author Christian Morgner
 */
public interface TraverserInterface {
	
	public TraversalDescription getTraversalDescription(SecurityContext securityContext, Object sourceProperty);
	public List transformResult(List<AbstractNode> traversalResult);
	public void addPredicate(Predicate<Node> predicate);
	public Comparator<AbstractNode> getComparator();
	public Notion getNotion();
	public void cleanup();
	public boolean collapseSingleResult();
}
