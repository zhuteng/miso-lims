package uk.ac.bbsrc.tgac.miso.sqlstore.util.interceptors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

public class PoolInterceptor extends EmptyInterceptor {

  // private Session session;
  private Map<String, Object> previousState = new HashMap<String, Object>();
  private Map<String, Object> currentState = new HashMap<String, Object>();

  /**
   * Only returns differences since the Pool was last saved.
   * @return
   * a map of differences that looks like:
   * {
   *    "fieldName": {
   *        "previous": obj,
   *        "current": obj
   *    },
   *    ...
   * }
   */
  public Map<String, Map<String, Object>> getDifferences() {
    final Map<String, Map<String, Object>> rtn = new HashMap<String, Map<String, Object>>();
    for (Entry<String, Object> pEntry : previousState.entrySet()) {
      final String key = pEntry.getKey();
      final Object value = pEntry.getValue();
      if (!"lastModified".equals(key) && !value.equals(currentState.get(key))) {
        Map<String, Object> innerMap = new HashMap<String, Object>();
        innerMap.put("previous", value);
        innerMap.put("current", currentState.get(key));
        rtn.put(key, innerMap);
      }
    }
    return rtn;
  }

  @Override
  public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames,
      Type[] types) {

    for (int i = 0; i < propertyNames.length; i++) {
      this.previousState.put(propertyNames[i], previousState[i]);
      this.currentState.put(propertyNames[i], currentState[i]);
    }

    return true;

  }
}