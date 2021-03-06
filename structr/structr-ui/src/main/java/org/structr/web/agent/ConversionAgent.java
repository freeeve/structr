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



package org.structr.web.agent;

import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.Services;
import org.structr.core.UnsupportedArgumentError;
import org.structr.core.agent.Agent;
import org.structr.core.agent.ConversionTask;
import org.structr.core.agent.ReturnValue;
import org.structr.core.agent.Task;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.Principal;
import org.structr.web.entity.CsvFile;
import org.structr.web.node.ConvertCsvToNodeListCommand;

//~--- JDK imports ------------------------------------------------------------

import java.util.logging.Level;
import java.util.logging.Logger;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author amorgner
 */
public class ConversionAgent extends Agent {

	private static final Logger logger = Logger.getLogger(ConversionAgent.class.getName());

	//~--- constructors ---------------------------------------------------

	public ConversionAgent() {

		setName("ConversionAgent");

	}

	//~--- methods --------------------------------------------------------

	@Override
	public ReturnValue processTask(Task task) {

		if (task instanceof ConversionTask) {

			ConversionTask ct = (ConversionTask) task;

			logger.log(Level.INFO, "Task found, starting conversion ...");
			convert(ct.getUser(), ct.getSourceNode(), ct.getTargetNodeClass());
			logger.log(Level.INFO, " done.");

		}

		return (ReturnValue.Success);

	}

	private void convert(final Principal user, final AbstractNode sourceNode, final Class targetClass) {

		// FIXME: superuser security context
		final SecurityContext securityContext = SecurityContext.getSuperUserInstance();

		if (sourceNode == null) {

			throw new UnsupportedArgumentError("Source node is null!");
		}

		if (sourceNode instanceof CsvFile) {

			try {

				Services.command(securityContext, ConvertCsvToNodeListCommand.class).execute(user, sourceNode, targetClass);

			} catch (FrameworkException fex) {

				fex.printStackTrace();

			}

		} else {

			throw new UnsupportedArgumentError("Source node type " + sourceNode.getType() + " not supported. This agent can convert only CSV files.");
		}
	}

	//~--- get methods ----------------------------------------------------

	@Override
	public Class getSupportedTaskType() {

		return (ConversionTask.class);

	}

}
