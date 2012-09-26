/*
 *  Copyright (C) 2010-2012 Axel Morgner
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



package org.structr.websocket.command;

import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.Services;
import org.structr.core.node.StructrTransaction;
import org.structr.core.node.TransactionCommand;
import org.structr.web.Importer;
import org.structr.websocket.StructrWebSocket;
import org.structr.websocket.message.MessageBuilder;
import org.structr.websocket.message.WebSocketMessage;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author Axel Morgner
 */
public class ImportCommand extends AbstractCommand {

	private static final Logger logger = Logger.getLogger(ImportCommand.class.getName());

	//~--- static initializers --------------------------------------------

	static {

		StructrWebSocket.addCommand(ImportCommand.class);
		StructrWebSocket.addCommand(PatchCommand.class);
		StructrWebSocket.addCommand(SortCommand.class);
		StructrWebSocket.addCommand(WrapInComponentCommand.class);
		StructrWebSocket.addCommand(ClonePageCommand.class);
		StructrWebSocket.addCommand(ChildrenCommand.class);
		StructrWebSocket.addCommand(ListCommand.class);
		StructrWebSocket.addCommand(MoveCommand.class);
		StructrWebSocket.addCommand(AddCommand.class);
		StructrWebSocket.addCommand(RemoveCommand.class);
		StructrWebSocket.addCommand(LinkCommand.class);
		StructrWebSocket.addCommand(CreateSimplePage.class);

	}

	//~--- methods --------------------------------------------------------

	@Override
	public void processMessage(WebSocketMessage webSocketData) {

		final SecurityContext securityContext = getWebSocket().getSecurityContext();
		Map<String, Object> properties        = webSocketData.getNodeData();
		final String address                  = (String) properties.get("address");
		final String name                     = (String) properties.get("name");
		final int timeout                     = Integer.parseInt((String) properties.get("timeout"));
		final boolean publicVisible           = Boolean.parseBoolean((String) properties.get("publicVisible"));
		final boolean authVisible             = Boolean.parseBoolean((String) properties.get("authVisible"));
		StructrTransaction transaction        = new StructrTransaction() {

			@Override
			public Object execute() throws FrameworkException {

				Importer pageImporter = new Importer(securityContext, address, name, timeout, publicVisible, authVisible);
				boolean parseOk       = pageImporter.parse();

				if (parseOk) {

					logger.log(Level.INFO, "Sucessfully parsed {0}", address);
					getWebSocket().send(MessageBuilder.status().code(200).message("Sucessfully parsed address " + address).build(), true);

					String pageId                  = pageImporter.readPage();
					Map<String, Object> resultData = new HashMap<String, Object>();

					if (pageId != null) {

						resultData.put("id", pageId);
						getWebSocket().send(MessageBuilder.status().code(200).message("Sucessfully created page " + name).data(resultData).build(), true);

					} else {

						getWebSocket().send(MessageBuilder.status().code(400).message("Error while creating page " + name).data(resultData).build(), true);
					}

				}

				return null;

			}

		};

		try {

			Services.command(securityContext, TransactionCommand.class).execute(transaction);

		} catch (FrameworkException fex) {

			logger.log(Level.WARNING, "Error while importing content", fex);
			getWebSocket().send(MessageBuilder.status().code(fex.getStatus()).message(fex.getMessage()).build(), true);

		}

	}

	//~--- get methods ----------------------------------------------------

	@Override
	public String getCommand() {

		return "IMPORT";

	}

}
