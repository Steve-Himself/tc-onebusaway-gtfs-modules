/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.gtfs.serialization;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.CsvEntityReader;
import org.onebusaway.csv_entities.CsvInputSource;
import org.onebusaway.csv_entities.CsvTokenizerStrategy;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.impl.ZipHandler;
import org.onebusaway.gtfs.model.*;
import org.onebusaway.gtfs.services.GenericMutableDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GtfsReader extends CsvEntityReader {

  private final Logger _log = LoggerFactory.getLogger(GtfsReader.class);

  public static final String KEY_CONTEXT = GtfsReader.class.getName()
      + ".context";

  private List<Class<?>> _entityClasses = new ArrayList<Class<?>>();

  private GtfsReaderContextImpl _context = new GtfsReaderContextImpl();

  private GenericMutableDao _entityStore = new GtfsDaoImpl();

  private List<Agency> _agencies = new ArrayList<Agency>();

  private Map<Class<?>, Map<String, String>> _agencyIdsByEntityClassAndId = new HashMap<Class<?>, Map<String, String>>();

  private String _defaultAgencyId;

  private String _defaultStopAgencyId;

  private Map<String, String> _agencyIdMapping = new HashMap<String, String>();

  private boolean _overwriteDuplicates = false;

  private File _inputLocation = null;

  public GtfsReader() {
    this(Map.of());
  }
  public GtfsReader(Map<Class<? extends org.onebusaway.csv_entities.HasExtensions>, Class<?>> extensions) {
    this(List.of(), extensions);
  }

  public GtfsReader(List<Class<?>> entityClasses, Map<Class<? extends org.onebusaway.csv_entities.HasExtensions>, Class<?>> extensions) {
    List<Class<?>> classes = new ArrayList<>(
            List.of(
                    Agency.class,
                    Block.class,
                    ShapePoint.class,
                    Note.class,
                    Area.class,
                    BookingRule.class,
                    Route.class,
                    RouteGroup.class,
                    RouteGroupPair.class,
                    RouteStop.class,
                    RouteShape.class,
                    Level.class,
                    Stop.class,
                    Location.class,
                    LocationGroupElement.class,
                    Trip.class,
                    StopAreaElement.class,
                    StopTime.class,
                    ServiceCalendar.class,
                    ServiceCalendarDate.class,
                    RiderCategory.class,
                    FareMedium.class,
                    FareProduct.class,
                    FareLegRule.class,
                    FareAttribute.class,
                    FareRule.class,
                    FareTransferRule.class,
                    Frequency.class,
                    Pathway.class,
                    Transfer.class,
                    FeedInfo.class,
                    Ridership.class,
                    Translation.class,
                    Vehicle.class,
                    Facility.class,
                    FacilityPropertyDefinition.class,
                    FacilityProperty.class,
                    RouteNameException.class,
                    DirectionNameException.class,
                    WrongWayConcurrency.class,
                    DirectionEntry.class));
    classes.addAll(entityClasses);
    _entityClasses = classes.stream().distinct().collect(Collectors.toList());

    CsvTokenizerStrategy tokenizerStrategy = new CsvTokenizerStrategy();
    tokenizerStrategy.getCsvParser().setTrimInitialWhitespace(true);
    setTokenizerStrategy(tokenizerStrategy);
    
    setTrimValues(true);

    /**
     * Prep the Entity Schema Factories
     */
    DefaultEntitySchemaFactory schemaFactory = createEntitySchemaFactory();
    setEntitySchemaFactory(schemaFactory);

    extensions.forEach(schemaFactory::addExtension);

    CsvEntityContext ctx = getContext();
    ctx.put(KEY_CONTEXT, _context);

    addEntityHandler(new EntityHandlerImpl());
  }

  public void setInputLocation(File path) throws IOException {
    super.setInputLocation(path);
    _inputLocation = path;
  }

  public void setLastModifiedTime(Long lastModifiedTime) {
    if (lastModifiedTime != null)
      getContext().put("lastModifiedTime", lastModifiedTime);
  }
  public Long getLastModfiedTime() {
    return (Long)getContext().get("lastModifiedTime");
  }

  public List<Agency> getAgencies() {
    return _agencies;
  }

  public void setAgencies(List<Agency> agencies) {
    _agencies = new ArrayList<Agency>(agencies);
  }

  public void setDefaultAgencyId(String feedId) {
    _defaultAgencyId = feedId;
  }

  public String getDefaultAgencyId() {
    if (_defaultAgencyId != null)
      return _defaultAgencyId;
    if (_agencies.size() > 0)
      return _agencies.get(0).getId();
    throw new NoDefaultAgencyIdException();
  }

  public void setDefaultStopAgencyId(String feedId) {
    _defaultStopAgencyId = feedId;
  }

  public String getDefaultStopAgencyId() {
    if (_defaultStopAgencyId != null)
      return _defaultStopAgencyId;

    return getDefaultAgencyId();
  }


  public void addAgencyIdMapping(String fromAgencyId, String toAgencyId) {
    _agencyIdMapping.put(fromAgencyId, toAgencyId);
  }

  public GtfsReaderContext getGtfsReaderContext() {
    return _context;
  }
  
  public GenericMutableDao getEntityStore() {
    return _entityStore;
  }

  public void setEntityStore(GenericMutableDao entityStore) {
    _entityStore = entityStore;
  }

  public List<Class<?>> getEntityClasses() {
    return _entityClasses;
  }

  public void setEntityClasses(List<Class<?>> entityClasses) {
    _entityClasses = entityClasses;
  }

  public void setOverwriteDuplicates(boolean overwriteDuplicates) {
    _overwriteDuplicates = overwriteDuplicates;
  }

  public void readEntities(Class<?> entityClass, Reader reader) throws IOException, CsvEntityIOException {
    if (entityClass == Location.class) {
      for (Location location : new LocationsGeoJSONReader(reader, getDefaultAgencyId()).read()) {
        injectEntity(location);
      }
    } else {
      super.readEntities(entityClass, reader);
    }
  }

  public void run() throws IOException {
    run(getInputSource());
  }

  public void run(CsvInputSource source) throws IOException {

    List<Class<?>> classes = getEntityClasses();

    _entityStore.open();

    for (Class<?> entityClass : classes) {
      _log.info("reading entities: " + entityClass.getName());

      readEntities(entityClass, source);
      _entityStore.flush();
    }

    _entityStore.close();

    // support metadata files that are not CSV
    // but only if we have a GtfsDao
    if (_entityStore instanceof GtfsDaoImpl) {
      List<String> filenames = ((GtfsDaoImpl) _entityStore).getOptionalMetadataFilenames();
      if (filenames != null) {
        for (String metaFile : filenames) {
          if (source.hasResource(metaFile)) {
            _log.info("reading metadata file: " + metaFile);
            ((GtfsDaoImpl) _entityStore).addMetadata(metaFile, readContent(_inputLocation, metaFile));
          }
        }
      }
    }
  }

  private String readContent(File inputLocation, String filename) {
    if (inputLocation.getAbsoluteFile().getName().endsWith(".zip")) {
      // zip file
      return readContentFromZip(inputLocation,
        filename);
    } else {
      // file in directory
      return readContentFromFile(new File(inputLocation.getAbsolutePath()
              + File.separator
              + filename));
    }
  }

  private String readContentFromFile(File filePath) {
    StringBuffer sb = new StringBuffer();
    try {
      byte[] bytes = Files.readAllBytes(filePath.toPath());
      sb.append(new String(bytes, StandardCharsets.UTF_8));
    } catch (IOException e) {
      System.err.println("issue reading content from " + filePath);
    }
    return sb.toString();
  }

  private String readContentFromZip(File zipFilePath, String zipEntryName) {
    try {
      ZipHandler zip = new ZipHandler(zipFilePath);
      return zip.readTextFromFile(zipEntryName);
    } catch (IOException e) {
      System.err.println("issue reading content from " + zipFilePath + ":" + zipEntryName);
    }
    return null;
  }

  /****
   * Protected Methods
   ****/

  protected DefaultEntitySchemaFactory createEntitySchemaFactory() {
    return GtfsEntitySchemaFactory.createEntitySchemaFactory();
  }

  protected Object getEntity(Class<?> entityClass, Serializable id) {
    if (entityClass == null)
      throw new IllegalArgumentException("entity class must not be null");
    if (id == null)
      throw new IllegalArgumentException("entity id must not be null");
    return _entityStore.getEntityForId(entityClass, id);
  }

  protected String getTranslatedAgencyId(String agencyId) {
    String id = _agencyIdMapping.get(agencyId);
    if (id != null)
      return id;
    return agencyId;
  }

  protected String getAgencyForEntity(Class<?> entityType, String entityId) {

    Map<String, String> agencyIdsByEntityId = _agencyIdsByEntityClassAndId.get(entityType);

    if (agencyIdsByEntityId != null) {
      String id = agencyIdsByEntityId.get(entityId);
      if (id != null)
        return id;
    }

    throw new EntityReferenceNotFoundException(entityType, entityId);
  }


    /****
   * Private Internal Classes
   ****/

  private class EntityHandlerImpl implements EntityHandler {

    public void handleEntity(Object entity) {

      if (entity instanceof Agency) {
        Agency agency = (Agency) entity;
        if (agency.getId() == null) {
          if (_defaultAgencyId == null)
            agency.setId(agency.getName());
          else
            agency.setId(_defaultAgencyId);
        }

        // If we already have this agency from a previous load, then we don't
        // add it or save it to the entity store
        if (_agencies.contains(agency))
          return;

        _agencies.add((Agency) entity);
      } else if (entity instanceof BookingRule) {
        BookingRule bookingRule = (BookingRule) entity;
        registerAgencyId(BookingRule.class, bookingRule.getId());
      } else if (entity instanceof Pathway) {
        Pathway pathway = (Pathway) entity;
        registerAgencyId(Pathway.class, pathway.getId());
      } else if (entity instanceof Level) {
        Level level = (Level) entity;
        registerAgencyId(Level.class, level.getId());
      } else if (entity instanceof RouteGroup) {
        RouteGroup routeGroup = (RouteGroup) entity;
        registerAgencyId(RouteGroup.class, routeGroup.getId());
      } else if (entity instanceof Route) {
        Route route = (Route) entity;
        registerAgencyId(Route.class, route.getId());
      } else if (entity instanceof Trip) {
        Trip trip = (Trip) entity;
        registerAgencyId(Trip.class, trip.getId());
      } else if (entity instanceof Stop) {
        Stop stop = (Stop) entity;
        registerAgencyId(Stop.class, stop.getId());
      } else if (entity instanceof FareProduct) {
        FareProduct product = (FareProduct) entity;
        registerAgencyId(FareProduct.class, product.getId());
      } else if (entity instanceof FareMedium) {
        FareMedium medium = (FareMedium) entity;
        registerAgencyId(FareMedium.class, medium.getId());
      } else if (entity instanceof RiderCategory) {
        RiderCategory category = (RiderCategory) entity;
        registerAgencyId(RiderCategory.class, category.getId());
      } else if (entity instanceof FareAttribute) {
        FareAttribute fare = (FareAttribute) entity;
        registerAgencyId(FareAttribute.class, fare.getId());
      } else if (entity instanceof Note) {
        Note note = (Note) entity;
        registerAgencyId(Note.class, note.getId());
      } else if (entity instanceof Area) {
        Area area = (Area) entity;
        registerAgencyId(Area.class, area.getId());

      } else if (entity instanceof Location) {
        Location location = (Location) entity;
        registerAgencyId(Location.class, location.getId());
      } else if (entity instanceof LocationGroupElement) {
        LocationGroupElement locationGroupElement = (LocationGroupElement) entity;
        LocationGroup locationGroup = _entityStore.getEntityForId(LocationGroup.class, locationGroupElement.getLocationGroupId());
        if (locationGroup == null) {
          locationGroup = new LocationGroup();
          locationGroup.setId(locationGroupElement.getLocationGroupId());
          locationGroup.setName(locationGroupElement.getName());
          _entityStore.saveEntity(locationGroup);
        }
        locationGroup.addLocation(locationGroupElement.getLocation());
      } else if (entity instanceof StopAreaElement) {
        var stopAreaElement = (StopAreaElement) entity;
        var stopArea = _entityStore.getEntityForId(StopArea.class, stopAreaElement.getArea().getId());
        if (stopArea == null) {
          stopArea = new StopArea();
          stopArea.setArea(stopAreaElement.getArea());
          _entityStore.saveEntity(stopArea);
        }
        stopArea.addLocation(stopAreaElement.getStopLocation());
      } else if (entity instanceof Vehicle) {
        Vehicle vehicle = (Vehicle) entity;
        registerAgencyId(Vehicle.class, vehicle.getId());
      } else if (entity instanceof Facility){
        Facility facility = (Facility) entity;
        registerAgencyId(Facility.class, facility.getId());
      } else if (entity instanceof FacilityPropertyDefinition){
        FacilityPropertyDefinition facilityPropertyDefinition = (FacilityPropertyDefinition) entity;
        registerAgencyId(FacilityPropertyDefinition.class, facilityPropertyDefinition.getId());
      }

      if (entity instanceof IdentityBean<?>) {
        _entityStore.saveEntity(entity);
      }

    }

    private void registerAgencyId(Class<?> entityType, AgencyAndId id) {

      Map<String, String> agencyIdsByEntityId = _agencyIdsByEntityClassAndId.get(entityType);

      if (agencyIdsByEntityId == null) {
        agencyIdsByEntityId = new HashMap<String, String>();
        _agencyIdsByEntityClassAndId.put(entityType, agencyIdsByEntityId);
      }

      if (agencyIdsByEntityId.containsKey(id.getId()) && !_overwriteDuplicates) {
        throw new DuplicateEntityException(entityType, id);
      }

      agencyIdsByEntityId.put(id.getId(), id.getAgencyId());
    }
  }

  private class GtfsReaderContextImpl implements GtfsReaderContext {

    public Object getEntity(Class<?> entityClass, Serializable id) {
      return GtfsReader.this.getEntity(entityClass, id);
    }

    public String getDefaultAgencyId() {
      return GtfsReader.this.getDefaultAgencyId();
    }

    public String getDefaultStopAgencyId() {
      return GtfsReader.this.getDefaultStopAgencyId();
    }

    public List<Agency> getAgencies() {
      return GtfsReader.this.getAgencies();
    }

    public String getAgencyForEntity(Class<?> entityType, String entityId) {
      return GtfsReader.this.getAgencyForEntity(entityType, entityId);
    }

    public String getTranslatedAgencyId(String agencyId) {
      return GtfsReader.this.getTranslatedAgencyId(agencyId);
    }
  }
}
