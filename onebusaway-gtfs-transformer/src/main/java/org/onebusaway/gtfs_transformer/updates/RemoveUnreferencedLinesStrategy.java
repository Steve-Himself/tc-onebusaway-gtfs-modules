package org.onebusaway.gtfs_transformer.updates;

import org.onebusaway.gtfs.model.RouteGroup;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveUnreferencedLinesStrategy implements GtfsTransformStrategy {
	private static Logger _log = LoggerFactory.getLogger(RemoveUnreferencedLinesStrategy.class);

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	  public void run(TransformContext context, GtfsMutableRelationalDao dao) {

	    int removedLineCount = 0;

	      for (RouteGroup routeGroup : dao.getRouteGroups()) {

	          if (routeGroup.getRoutes().isEmpty()) {
	        	  dao.removeEntity(routeGroup);
				  removedLineCount++;
	      }
	    }

	    _log.info("removed=" + removedLineCount);

	    UpdateLibrary.clearDaoCache(dao);
	  }
}
