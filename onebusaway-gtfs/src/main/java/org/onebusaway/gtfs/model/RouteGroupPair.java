package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.RouteGroupIdFieldMappingFactory;

@CsvFields(filename = "route_group_pairs.txt", required = false)
public final class RouteGroupPair extends IdentityBean<String> {
  @CsvField(mapping = RouteGroupIdFieldMappingFactory.class)
  private String id;

  @CsvField(name = "route_id", mapping = EntityFieldMappingFactory.class)
  private Route route;

  @CsvField(name = "route_group_id", mapping = EntityFieldMappingFactory.class)
  private RouteGroup routeGroup;

  @CsvField(name = "is_default", optional = true)
  private boolean isDefault;

  public RouteGroupPair() {
  }

  public RouteGroupPair(RouteGroupPair r) {
    this.id = r.id;
    this.route = r.route;
    this.routeGroup = r.routeGroup;
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

  public RouteGroup getRouteGroup() {
    return routeGroup;
  }

  public void setRouteGroup(RouteGroup routeGroup) {
    this.routeGroup = routeGroup;
  }

  public boolean getIsDefault() {
    return isDefault;
  }

  public void setIsDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  @Override
  public String toString() {
    return "<RouteGroups " + id + ">";
  }

}
