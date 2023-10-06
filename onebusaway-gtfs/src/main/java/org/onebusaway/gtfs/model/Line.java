package org.onebusaway.gtfs.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.RouteAgencyFieldMappingFactory;

import java.util.HashSet;
import java.util.Set;

@CsvFields(filename = "lines_ext.txt", prefix = "line_", required = false)
public final class Line extends IdentityBean<AgencyAndId> {

  private static final long serialVersionUID = 1L;

  private static final int MISSING_VALUE = -999;

  @CsvField(mapping = DefaultAgencyIdFieldMappingFactory.class)
  private AgencyAndId id;

  @CsvField(name = "agency_id", optional = true, mapping = RouteAgencyFieldMappingFactory.class, order = -1)
  private Agency agency;

  @CsvField(optional = true, alwaysIncludeInOutput = true)
  private String shortName;

  @CsvField(optional = true, alwaysIncludeInOutput = true)
  private String longName;

  @CsvField(optional = true)
  private String url;

  @CsvField(optional = true)
  private String color;

  @CsvField(optional = true)
  private String textColor;

  @CsvField(optional = true)
  private int sortOrder = MISSING_VALUE;

  @CsvField(ignore = true)
  private Set<Route> routes = new HashSet<>();

  public Line() {

  }

  public Line(Line r) {
    this.id = r.id;
    this.agency = r.agency;
    this.shortName = r.shortName;
    this.longName = r.longName;
    this.url = r.url;
    this.color = r.color;
    this.textColor = r.textColor;
    this.sortOrder = r.sortOrder;
  }

  public AgencyAndId getId() {
    return id;
  }

  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public Agency getAgency() {
    return agency;
  }

  public void setAgency(Agency agency) {
    this.agency = agency;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getLongName() {
    return longName;
  }

  public void setLongName(String longName) {
    this.longName = longName;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getTextColor() {
    return textColor;
  }

  public void setTextColor(String textColor) {
    this.textColor = textColor;
  }

  public boolean isSortOrderSet() {
    return sortOrder != MISSING_VALUE;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
  }

  public Set<Route> getRoutes() {
    return routes;
  }

  public void setRoutes(Set<Route> routes) {
    this.routes = routes;
  }

  @Override
  public String toString() {
    return "<Line " + id + " " + shortName + ">";
  }

}
