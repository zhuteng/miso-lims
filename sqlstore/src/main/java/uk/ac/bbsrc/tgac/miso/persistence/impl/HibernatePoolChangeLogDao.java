package uk.ac.bbsrc.tgac.miso.persistence.impl;

import uk.ac.bbsrc.tgac.miso.core.data.impl.PoolChangeLog;
import uk.ac.bbsrc.tgac.miso.sqlstore.util.AbstractHibernateDao;

public class HibernatePoolChangeLogDao extends AbstractHibernateDao {

  public void save(PoolChangeLog changeLog) {
    getCurrentSession().saveOrUpdate(changeLog);
  }

}
