/*
 *  Copyright (C) 2010-2012 Axel Morgner, structr <structr@structr.org>
 *
 *  This file is part of structr <http://structr.org>.
 *
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */



package org.structr.web.entity.relation;

import org.structr.common.PropertyKey;
import org.structr.common.PropertyView;
import org.structr.common.RelType;
import org.structr.core.EntityContext;
import org.structr.core.entity.AbstractRelationship;
import org.structr.web.entity.Element;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author axel
 */
public class ChildrenRelationship extends AbstractRelationship {

	static {

		EntityContext.registerNamedRelation("children", ChildrenRelationship.class, Element.class, Element.class, RelType.CONTAINS);
		EntityContext.registerPropertySet(ChildrenRelationship.class, PropertyView.Ui, Key.values());

	}

	//~--- constant enums -------------------------------------------------

	public enum Key implements PropertyKey{ parentId, childId }

	//~--- get methods ----------------------------------------------------

	@Override
	public PropertyKey getStartNodeIdKey() {

		return Key.parentId;

	}

	@Override
	public PropertyKey getEndNodeIdKey() {

		return Key.childId;

	}

}
