package org.onebusaway.gtfs.serialization.mappings;

import org.onebusaway.csv_entities.CsvEntityContext;
import org.onebusaway.csv_entities.exceptions.MissingRequiredFieldException;
import org.onebusaway.csv_entities.schema.*;

import java.util.Map;

public class RouteLineIdFieldMappingFactory implements FieldMappingFactory {

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

      String lineId = (String) csvValues.get("line_id");
      String routeId = (String) csvValues.get("route_id");
      boolean missing = (lineId == null || lineId.isEmpty())
          || (routeId == null || routeId.isEmpty());
      if (missing) {
        if (_required)
          throw new MissingRequiredFieldException(_entityType, _csvFieldName);
        return;
      }
      String id = lineId + ":" + routeId;
      object.setPropertyValue(_objFieldName, id);
    }
  }
}
