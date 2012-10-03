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

import org.structr.common.RelType;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.Command;
import org.structr.core.Services;
import org.structr.core.entity.AbstractNode;
import org.structr.core.node.*;
import org.structr.core.node.CreateNodeCommand;
import org.structr.core.node.StructrTransaction;
import org.structr.core.node.TransactionCommand;
import org.structr.web.entity.Content;
import org.structr.web.entity.Element;
import org.structr.web.entity.Page;
import org.structr.web.entity.html.Body;
import org.structr.web.entity.html.Div;
import org.structr.web.entity.html.H1;
import org.structr.web.entity.html.Head;
import org.structr.web.entity.html.Html;
import org.structr.web.entity.html.HtmlElement;
import org.structr.web.entity.html.Title;
import org.structr.websocket.message.MessageBuilder;
import org.structr.websocket.message.WebSocketMessage;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

//~--- classes ----------------------------------------------------------------

/**
 * Websocket command to create a simple HTML page
 *
 * @author Axel Morgner
 */
public class CreateSimplePage extends AbstractCommand {

	private static final Logger logger = Logger.getLogger(WrapInComponentCommand.class.getName());
	private static Command createNode;
	private static Command createRel;
	private static String pageName;

	//~--- methods --------------------------------------------------------

	@Override
	public void processMessage(final WebSocketMessage webSocketData) {

		pageName = (String) webSocketData.getNodeData().get(Page.UiKey.name.name());

		final SecurityContext securityContext = getWebSocket().getSecurityContext();

		createNode = Services.command(securityContext, CreateNodeCommand.class);
		createRel  = Services.command(securityContext, CreateRelationshipCommand.class);

		StructrTransaction transaction = new StructrTransaction() {

			@Override
			public Object execute() throws FrameworkException {

				Page page = (Page) createElement(pageName, null, Page.class.getSimpleName(), 0, null);

				page.setProperty(Page.UiKey.contentType, "text/html");

				Html html   = (Html) createElement(null, page, Html.class.getSimpleName(), 0, page);
				Head head   = (Head) createElement(null, page, Head.class.getSimpleName(), 0, html);
				Body body   = (Body) createElement(null, page, Body.class.getSimpleName(), 1, html);
				Title title = (Title) createElement(null, page, Title.class.getSimpleName(), 0, head);

				// nodeData.put(Content.UiKey.content.name(), "Page Title");
				Content content = (Content) createElement(null, page, Content.class.getSimpleName(), 0, title);

				// nodeData.remove(Content.UiKey.content.name());
				content.setProperty(Content.UiKey.content, "Page Title");

				H1 h1 = (H1) createElement(null, page, H1.class.getSimpleName(), 0, body);

				// nodeData.put(Content.UiKey.content.name(), "Page Title");
				Content h1Content = (Content) createElement(null, page, Content.class.getSimpleName(), 0, h1);

				// nodeData.remove(Content.UiKey.content.name());
				h1Content.setProperty(Content.UiKey.content, "Page Title");

				Div div = (Div) createElement(null, page, Div.class.getSimpleName(), 1, body);

				// nodeData.put(Content.UiKey.content.name(), "Body Text");
				Content divContent = (Content) createElement(null, page, Content.class.getSimpleName(), 0, div);

				divContent.setProperty(Content.UiKey.content, "Body Text");

//                              
//
//                              Map<String, Object> relData = new HashMap<String, Object>();
//                              String pageId               = page.getUuid();
//                              relData.put(pageId, 0);
//                              
//                              // Html
//                              nodeData.put(AbstractNode.Key.type.name(), Html.class.getSimpleName());
//                              nodeData.put(HtmlElement.UiKey.name.name(), "html");
//                              nodeData.put(HtmlElement.UiKey.tag.name(), "html");
//                              AbstractNode html = (AbstractNode) createNode.execute(nodeData);
//                              createRel.execute(page, html, RelType.CONTAINS, relData, false);
//                              
//                              // Html -> Head
//                              nodeData.put(AbstractNode.Key.type.name(), Head.class.getSimpleName());
//                              nodeData.put(HtmlElement.UiKey.name.name(), "head");
//                              nodeData.put(HtmlElement.UiKey.tag.name(), "head");
//                              AbstractNode head = (AbstractNode) createNode.execute(nodeData);
//                              createRel.execute(html, head, RelType.CONTAINS, relData, false);
//                              
//                              // Html -> Body
//                              nodeData.put(AbstractNode.Key.type.name(), Body.class.getSimpleName());
//                              nodeData.put(HtmlElement.UiKey.name.name(), "body");
//                              nodeData.put(HtmlElement.UiKey.tag.name(), "body");
//                              AbstractNode body = (AbstractNode) createNode.execute(nodeData);
//                              relData.put(pageId, 1);
//                              createRel.execute(html, body, RelType.CONTAINS, relData, false);
//                              
//                              // Html -> Head -> Title
//                              relData.put(pageId, 0);
//                              nodeData.put(AbstractNode.Key.type.name(), Title.class.getSimpleName());
//                              nodeData.put(HtmlElement.UiKey.name.name(), "title");
//                              nodeData.put(HtmlElement.UiKey.tag.name(), "title");
//                              AbstractNode title = (AbstractNode) createNode.execute(nodeData);
//                              createRel.execute(head, title, RelType.CONTAINS, relData, false);
//
//                              // Html -> Head -> Title -> Content
//                              nodeData.put(AbstractNode.Key.type.name(), Content.class.getSimpleName());
//                              nodeData.remove(HtmlElement.UiKey.tag.name());
//                              nodeData.put(Content.UiKey.content.name(), "New Page");
//                              //nodeData.put(HtmlElement.UiKey.tag.name(), "body");
//                              AbstractNode content = (AbstractNode) createNode.execute(nodeData);
//                              relData.put(pageId, 0);
//                              createRel.execute(title, content, RelType.CONTAINS, relData, false);
//                              
//                              // Html -> Body -> H1
//                              relData.put(pageId, 0);
//                              nodeData.put(AbstractNode.Key.type.name(), H1.class.getSimpleName());
//                              nodeData.put(HtmlElement.UiKey.name.name(), "h1");
//                              nodeData.put(HtmlElement.UiKey.tag.name(), "h1");
//                              AbstractNode h1 = (AbstractNode) createNode.execute(nodeData);
//                              createRel.execute(body, h1, RelType.CONTAINS, relData, false);
//                              
				return page;

			}

		};

		try {

			// create nodes in transaction
			Services.command(securityContext, TransactionCommand.class).execute(transaction);
		} catch (FrameworkException fex) {

			logger.log(Level.WARNING, "Could not create node.", fex);
			getWebSocket().send(MessageBuilder.status().code(fex.getStatus()).message(fex.toString()).build(), true);

		}

	}

	private AbstractNode createElement(final String name, final AbstractNode page, final String type, final int position, final AbstractNode parentElement) throws FrameworkException {

		Map<String, Object> nodeData = new HashMap<String, Object>();

		nodeData.put(AbstractNode.Key.visibleToAuthenticatedUsers.name(), true);
		nodeData.put(AbstractNode.Key.type.name(), Page.class.getSimpleName());

		Map<String, Object> relData = new HashMap<String, Object>();

		if (parentElement != null) {

			Set<String> parentPaths = (Set<String>) parentElement.getProperty(Element.UiKey.paths);

			if (parentPaths != null && !parentPaths.isEmpty()) {

				String parentTreeAddress = parentPaths.iterator().next();

				relData.put(parentTreeAddress, position);

			} else {

				relData.put(page.getUuid(), position);
			}

		}

		nodeData.put(AbstractNode.Key.type.name(), type);
		nodeData.put(HtmlElement.UiKey.name.name(), name != null
			? name
			: type.toLowerCase());

		if (!Content.class.getSimpleName().equals(type)) {

			nodeData.put(HtmlElement.UiKey.tag.name(), type.toLowerCase());
		}

		AbstractNode element = (AbstractNode) createNode.execute(nodeData);

		if (parentElement != null) {

			createRel.execute(parentElement, element, RelType.CONTAINS, relData, false);
		}

		return element;

	}

	//~--- get methods ----------------------------------------------------

	@Override
	public String getCommand() {

		return "CREATE_SIMPLE_PAGE";

	}

}
