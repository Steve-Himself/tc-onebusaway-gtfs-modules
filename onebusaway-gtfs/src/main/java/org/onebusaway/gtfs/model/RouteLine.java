package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.RouteLineIdFieldMappingFactory;

@CsvFields(filename = "route_lines_ext.txt", prefix = "route_line_", required = false)
public final class RouteLine extends IdentityBean<String> {
  @CsvField(mapping = RouteLineIdFieldMappingFactory.class)
  private String id;

  @CsvField(name = "route_id", mapping = EntityFieldMappingFactory.class)
  private Route route;

  @CsvField(name = "line_id", mapping = EntityFieldMappingFactory.class)
  private Line line;

  @CsvField(name = "is_default", optional = true)
  private boolean isDefault;

  public RouteLine() {
  }

  public RouteLine(RouteLine r) {
    this.id = r.id;
    this.route = r.route;
    this.line = r.line;
    this.isDefault = r.isDefault;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Route getRoute() {
    return route;
  }

  public void setRoute(Route route) {
    this.route = route;
  }

  public Line getLine() {
    return line;
  }

  public void setLine(Line line) {
    this.line = line;
  }

  public boolean getIsDefault() {
    return isDefault;
  }

  public void setIsDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  @Override
  public String toString() {
    return "<RouteLine " + id + ">";
  }

}
