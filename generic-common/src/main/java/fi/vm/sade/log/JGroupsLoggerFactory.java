package fi.vm.sade.log;

import org.jgroups.logging.CustomLogFactory;
import org.jgroups.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JGroupsLoggerFactory implements CustomLogFactory {
	@Override
	@SuppressWarnings("rawtypes")
	public Log getLog(Class clazz) {
		Logger logger = LoggerFactory.getLogger(clazz);
		return new JGroupsCustomLog(logger);
	}

	@Override
	public Log getLog(String category) {
		Logger logger = LoggerFactory.getLogger(category);
		return new JGroupsCustomLog(logger);
	}
}
