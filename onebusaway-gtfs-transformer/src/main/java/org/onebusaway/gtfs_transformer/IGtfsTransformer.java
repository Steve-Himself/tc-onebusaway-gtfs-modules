package org.onebusaway.gtfs_transformer;

import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsEntityTransformStrategy;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;

public interface IGtfsTransformer {
    GtfsReader getReader();
    GtfsRelationalDao getDao();
    void addTransform(GtfsTransformStrategy strategy);
    void addEntityTransform(GtfsEntityTransformStrategy entityTransform);
    GtfsTransformStrategy getLastTransform();
}
