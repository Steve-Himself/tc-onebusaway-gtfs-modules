package org.onebusaway.gtfs.serialization.mappings;

import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.exceptions.MissingRequiredFieldException;
import org.onebusaway.csv_entities.schema.*;

import java.util.Map;

public class RouteGroupIdFieldMappingFactory implements FieldMappingFactory {

  public FieldMapping createFieldMapping(EntitySchemaFactory schemaFactory,
      Class<?> entityType, String csvFieldName, String objFieldName,
      Class<?> objFieldType, boolean required) {

    return new FieldMappingImpl(entityType, csvFieldName, objFieldName,
        String.class, required);
  }

  private class FieldMappingImpl extends DefaultFieldMapping {

    public FieldMappingImpl(Class<?> entityType, String csvFieldName,
        String objFieldName, Class<?> objFieldType, boolean required) {
      super(entityType, csvFieldName, objFieldName, objFieldType, required);
    }

    @Override
    public void translateFromCSVToObject(CsvEntityContext context,
        Map<String, Object> csvValues, BeanWrapper object) {

      String routeGroupId = (String) csvValues.get("route_group_id");
      String routeId = (String) csvValues.get("route_id");
      boolean missing = (routeGroupId == null || routeGroupId.isEmpty())
          || (routeId == null || routeId.isEmpty());
      if (missing) {
        if (_required)
          throw new MissingRequiredFieldException(_entityType, _csvFieldName);
        return;
      }
      String id = routeGroupId + ":" + routeId;
      object.setPropertyValue(_objFieldName, id);
    }
  }
}
