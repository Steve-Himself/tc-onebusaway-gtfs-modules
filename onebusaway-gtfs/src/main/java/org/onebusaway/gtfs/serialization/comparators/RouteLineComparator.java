package org.onebusaway.gtfs.serialization.comparators;

import org.onebusaway.gtfs.model.RouteLine;

import java.util.Comparator;

public class RouteLineComparator implements Comparator<RouteLine>{
  @Override
  public int compare(RouteLine o1, RouteLine o2) {
    String id1 = o1.getId();
    String id2 = o2.getId();
    return id1.compareTo(id2);
  }
}