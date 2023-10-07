/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onebusaway.gtfs_transformer.impl;

import org.onebusaway.csv_entities.CsvEntityReader;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static org.onebusaway.gtfs_transformer.csv.CSVUtil.readCsv;

public class MergeRouteFromRouteExtStrategy implements GtfsTransformStrategy {

    private final Logger _log = LoggerFactory.getLogger(MergeRouteFromRouteExtStrategy.class);
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void run(TransformContext context, GtfsMutableRelationalDao dao) {
        try {
            var inputSource = context.getReader().getInputSource();

            if (inputSource.hasResource("routes_ext.txt")) {
                HashMap<String, Route> referenceRoutes = new HashMap<>();
                CsvEntityReader reader = new CsvEntityReader();
                reader.setInputSource(inputSource);
                reader.getContext().put(GtfsReader.KEY_CONTEXT, context.getReader().getGtfsReaderContext());

                for (Route route : readCsv(Route.class, "routes_ext.txt", reader)) {
                    referenceRoutes.put(route.getId().getId(), route);
                }
                int routeCount = 0;
                for (Route route : dao.getAllRoutes()) {
                    String identifier = route.getId().getId();

                    Route refRoute = referenceRoutes.get(identifier);
                    if (refRoute != null) {
                        routeCount++;
                        route.setAttributes(refRoute.getAttributes());
                    }
                }

                _log.info("found {} route extensions, matched {} routes",
                        referenceRoutes.size(), routeCount);
            }
        } catch (Exception e) {
            _log.error("An error occurred while processing route extension");
        }
    }
}

