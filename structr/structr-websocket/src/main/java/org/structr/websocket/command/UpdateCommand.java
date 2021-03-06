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



package org.structr.websocket.command;

import org.apache.commons.lang.StringUtils;

import org.structr.common.RelType;
import org.structr.common.error.FrameworkException;
import org.structr.core.GraphObject;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractRelationship;
import org.structr.websocket.message.MessageBuilder;
import org.structr.websocket.message.WebSocketMessage;

//~--- JDK imports ------------------------------------------------------------

import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author Christian Morgner
 */
public class UpdateCommand extends AbstractCommand {

	private static final Logger logger = Logger.getLogger(UpdateCommand.class.getName());

	//~--- methods --------------------------------------------------------

	@Override
	public void processMessage(WebSocketMessage webSocketData) {

		GraphObject obj  = getNode(webSocketData.getId());
		String recString = (String) webSocketData.getNodeData().get("recursive");
		
		webSocketData.getNodeData().remove("recursive");
		
		boolean rec      = StringUtils.isNotBlank(recString)
				   ? Boolean.parseBoolean(recString)
				   : false;

//              final Map<String, Object> relData = webSocketData.getRelData();
		if (obj == null) {

			// No node? Try to find relationship
			obj = getRelationship(webSocketData.getId());
		}

		if (obj != null) {

			for (Entry<String, Object> entry : webSocketData.getNodeData().entrySet()) {

				setPropertyRecursively(obj, entry.getKey(), entry.getValue(), rec);

			}

		} else {

			logger.log(Level.WARNING, "Graph object with uuid {0} not found.", webSocketData.getId());
			getWebSocket().send(MessageBuilder.status().code(404).build(), true);

		}
	}

	//~--- get methods ----------------------------------------------------

	@Override
	public String getCommand() {
		return "UPDATE";
	}

	//~--- set methods ----------------------------------------------------

	private void setPropertyRecursively(final GraphObject obj, final String key, final Object value, final boolean rec) {

		try {

			obj.setProperty(key, value);

			if (rec) {

				if (obj instanceof AbstractNode) {

					AbstractNode node = (AbstractNode) obj;

					for (AbstractRelationship rel : node.getOutgoingRelationships(RelType.CONTAINS)) {

						AbstractNode endNode = rel.getEndNode();

						if (endNode != null) {

							setPropertyRecursively(endNode, key, value, rec);

						}

					}

				}

			}

		} catch (FrameworkException fex) {
			fex.printStackTrace();
		}
	}
}
