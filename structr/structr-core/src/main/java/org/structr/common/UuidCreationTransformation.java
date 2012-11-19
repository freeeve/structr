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

import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.structr.common.error.FrameworkException;
import org.structr.core.GraphObject;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractRelationship;

/**
 *
 * @author Christian Morgner
 */
public class UuidCreationTransformation extends GraphObjectTransformation {

	@Override
	public void apply(SecurityContext securityContext, GraphObject obj) throws FrameworkException {

		// create uuid if not set
		String uuid = (String)obj.getProperty(AbstractNode.uuid);
		if(StringUtils.isBlank(uuid)) {
			
			String nextUuid = UUID.randomUUID().toString().replaceAll("[\\-]+", "");
				
			if (obj instanceof AbstractNode) {
				
				((AbstractNode)obj).setProperty(GraphObject.uuid, nextUuid, true);
				return;
			}

			if (obj instanceof AbstractRelationship) {
			
				((AbstractNode)obj).setProperty(GraphObject.uuid, nextUuid, true);
				return;
			}
		}
	}

	@Override
	public int getOrder() {
		
		// first
		return 0;
	}
}
