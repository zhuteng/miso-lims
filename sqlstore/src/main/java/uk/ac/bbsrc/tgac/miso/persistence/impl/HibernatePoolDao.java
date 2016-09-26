package uk.ac.bbsrc.tgac.miso.persistence.impl;

import static uk.ac.bbsrc.tgac.miso.core.util.LimsUtils.isStringEmptyOrNull;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.eaglegenomics.simlims.core.Note;
import com.eaglegenomics.simlims.core.SecurityProfile;
import com.eaglegenomics.simlims.core.User;

import uk.ac.bbsrc.tgac.miso.core.data.AbstractBoxable;
import uk.ac.bbsrc.tgac.miso.core.data.AbstractPool;
import uk.ac.bbsrc.tgac.miso.core.data.Boxable;
import uk.ac.bbsrc.tgac.miso.core.data.Experiment;
import uk.ac.bbsrc.tgac.miso.core.data.Pool;
import uk.ac.bbsrc.tgac.miso.core.data.Poolable;
import uk.ac.bbsrc.tgac.miso.core.data.impl.PoolChangeLog;
import uk.ac.bbsrc.tgac.miso.core.data.impl.PoolImpl;
import uk.ac.bbsrc.tgac.miso.core.data.type.PlatformType;
import uk.ac.bbsrc.tgac.miso.core.exception.MisoNamingException;
import uk.ac.bbsrc.tgac.miso.core.service.naming.MisoNamingScheme;
import uk.ac.bbsrc.tgac.miso.core.store.BoxStore;
import uk.ac.bbsrc.tgac.miso.core.store.NoteStore;
import uk.ac.bbsrc.tgac.miso.core.store.Store;
import uk.ac.bbsrc.tgac.miso.core.store.WatcherStore;
import uk.ac.bbsrc.tgac.miso.core.util.CoverageIgnore;
import uk.ac.bbsrc.tgac.miso.sqlstore.SQLPoolDAO;
import uk.ac.bbsrc.tgac.miso.sqlstore.util.DbUtils;
import uk.ac.bbsrc.tgac.miso.sqlstore.util.interceptors.PoolInterceptor;

@Transactional(rollbackFor = Exception.class)
@Repository
public class HibernatePoolDao {
  private static final String TABLE_NAME = "Pool";
  private static final String POOL_SELECT =
"SELECT " +
  "p.poolId as poolId,"+
  "p.concentration as concentration,"+
  "p.identificationBarcode as identificationBarcode,"+
  "p.name as name,"+
  "p.creationDate as creationDate,"+
  "p.securityProfile_profileId as securityProfile,"+
  "p.platformType as platformType,"+
  "p.ready as readyToRun,"+
  "p.alias as labelText,"+
  "bp.boxPositionId as positionId,"+
  "p.emptied as empty,"+
  "p.volume as volume,"+
  "p.qcPassed as qcPassed,"+
  "p.description as description,"+
  "pmod.lastModified as lastModified "+"FROM "+TABLE_NAME+" p "+"LEFT JOIN BoxPosition bp ON bp.boxPositionId = p.boxPositionId "+"LEFT JOIN Box b ON b.boxId = bp.boxId "+"LEFT JOIN (SELECT poolId, MAX(changeTime) AS lastModified FROM PoolChangeLog GROUP BY poolId) pmod ON p.poolId = pmod.poolId";
  public static final String DILUTION_POOL_SELECT_BY_RELATED_PROJECT = POOL_SELECT
      + " WHERE p.poolId IN (SELECT DISTINCT pool_poolId FROM Project p "

      + "INNER JOIN Sample sa ON sa.project_projectId = p.projectId " + "INNER JOIN Library li ON li.sample_sampleId = sa.sampleId "
      + "INNER JOIN LibraryDilution ld ON ld.library_libraryId = li.libraryId "
      + "LEFT JOIN Pool_Elements pld ON pld.elementId = ld.dilutionId "
      + "WHERE p.projectId = ? AND pld.elementType = 'uk.ac.bbsrc.tgac.miso.core.data.impl.LibraryDilution')";
  public static final String EMPCR_POOL_SELECT_BY_RELATED_PROJECT = POOL_SELECT
      + " WHERE p.poolId IN (SELECT DISTINCT pool_poolId FROM Project p " + "INNER JOIN Sample sa ON sa.project_projectId = p.projectId "
      + "INNER JOIN Library li ON li.sample_sampleId = sa.sampleId "
      + "INNER JOIN LibraryDilution ld ON ld.library_libraryId = li.libraryId "
      + "LEFT JOIN emPCR e ON e.dilution_dilutionId = ld.dilutionId " + "LEFT JOIN emPCRDilution ed ON ed.emPCR_pcrId = e.pcrId "
      + "LEFT JOIN Pool_Elements ple ON ple.elementId = ed.dilutionId "
      + "WHERE p.projectId = ? AND ple.elementType = 'uk.ac.bbsrc.tgac.miso.core.data.impl.emPCRDilution')";
  protected static final Logger log = LoggerFactory.getLogger(HibernatePoolDao.class);
  public static final String PLATE_POOL_SELECT_BY_RELATED_PROJECT = POOL_SELECT
      + " WHERE p.poolId IN (SELECT DISTINCT pool_poolId FROM Project p "

      + "INNER JOIN Sample sa ON sa.project_projectId = p.projectId " + "INNER JOIN Library li ON li.sample_sampleId = sa.sampleId "
      + "INNER JOIN Plate_Elements pe ON li.libraryId = pe.elementId " + "INNER JOIN Plate pl ON pl.plateId = pe.plate_plateId "
      + "LEFT JOIN Pool_Elements pld ON pld.elementId = pl.plateId " + "WHERE p.projectId= ? AND pld.elementType LIKE '%Plate')";
  public static final String POOL_ID_SELECT_BY_RELATED = "SELECT DISTINCT pool_poolId AS poolId FROM Pool_Elements, "
      + "(SELECT dilutionId as elementId, library_libraryId as libraryId, 'uk.ac.bbsrc.tgac.miso.core.data.impl.LibraryDilution' as elementType FROM LibraryDilution "
      + "UNION ALL SELECT emPCRDilution.dilutionId as elementId, library_libraryId as libraryId, 'uk.ac.bbsrc.tgac.miso.core.data.impl.emPCRDilution' as elementType "
      + "FROM LibraryDilution JOIN emPCR ON LibraryDilution.dilutionId = emPCR.dilution_dilutionId JOIN emPCRDilution ON emPCR.pcrId = emPCRDilution.emPCR_pcrID"
      + ") AS Contents WHERE Contents.elementType = Pool_Elements.elementType AND Contents.elementId = Pool_Elements.elementId ";
  public static final String POOL_ID_SELECT_BY_RELATED_LIBRARY = POOL_ID_SELECT_BY_RELATED + "AND Contents.libraryId = ?";
  public static final String POOL_ID_SELECT_BY_RELATED_SAMPLE = POOL_ID_SELECT_BY_RELATED
      + "AND Contents.libraryId IN (SELECT libraryId FROM Library WHERE sample_sampleId = ?)";

  public static final String POOL_SELECT_FROM_ID_LIST = POOL_SELECT + " WHERE p.poolId IN :ids";

  private boolean autoGenerateIdentificationBarcodes;

  private BoxStore boxDAO;

  private PoolInterceptor interceptor = new PoolInterceptor();

  @Autowired
  private MisoNamingScheme<Pool<? extends Poolable<?, ?>>> namingScheme;

  private NoteStore noteDAO;

  private HibernatePoolChangeLogDao poolChangeLogDAO;

  private Store<SecurityProfile> securityProfileDAO;

  private Session session;

  @Autowired
  private SessionFactory sessionFactory;

  // some of these queries don't make a great deal of sense in hibernate (yet).
  // offloading to original class.
  private SQLPoolDAO sqlPoolDAO;

  private WatcherStore watcherDAO;

  /**
   * Generates a unique barcode. Note that the barcode will change if the Platform is changed.
   *
   * @param pool
   */
  public void autoGenerateIdBarcode(Pool pool) {
    String barcode = pool.getName() + "::" + pool.getPlatformType().getKey();
    pool.setIdentificationBarcode(barcode);
  }

  @SuppressWarnings("rawtypes")
  public PoolImpl get(long poolId) {
    return (PoolImpl) getCriteriaForClass(PoolImpl.class).add(Restrictions.eq("poolId", poolId)).uniqueResult();
  }

  @CoverageIgnore
  public boolean getAutoGenerateIdentificationBarcodes() {
    return autoGenerateIdentificationBarcodes;
  }

  @CoverageIgnore
  public BoxStore getBoxDAO() {
    return boxDAO;
  }

  public PoolImpl getByBarcode(String barcode) {
    if (barcode == null) throw new NullPointerException("cannot look up null barcode");
    @SuppressWarnings("unchecked")
    List<PoolImpl> eResults = getCriteriaForClass(PoolImpl.class).add(Restrictions.eq("identificationBarcode", barcode)).list();
    PoolImpl e = eResults.size() > 0 ? (PoolImpl) eResults.get(0) : null;
    return e;
  }

  @SuppressWarnings("unchecked")
  public List<PoolImpl> getByBarcodeList(List<String> barcodeList) {
    return getCriteriaForClass(PoolImpl.class).add(Restrictions.in("identificationBarcode", barcodeList)).list();
  }

  public Boxable getByPositionId(long positionId) {
    @SuppressWarnings("unchecked")
    List<PoolImpl> eResults = getCriteriaForClass(PoolImpl.class).add(Restrictions.eq("positionId", positionId)).list();
    return eResults.size() > 0 ? eResults.get(0) : null;
  }

  public Criteria getCriteriaForClass(Class<?> clazz) {
    return getCurrentSession().createCriteria(clazz);
  }

  public Session getCurrentSession() {
    if (session == null) {
      session = sessionFactory.withOptions().interceptor(interceptor).openSession();
    }
    return session;

  }

  @CoverageIgnore
  public MisoNamingScheme<Pool<? extends Poolable<?, ?>>> getNamingScheme() {
    return namingScheme;
  }

  @CoverageIgnore
  public NoteStore getNoteDAO() {
    return noteDAO;
  }

  // TODO: make the column names constants
  public PoolImpl getPoolByBarcode(String barcode, PlatformType platformType) throws IOException {
    if (barcode == null) throw new NullPointerException("cannot look up null barcode");
    if (platformType == null) {
      return getByBarcode(barcode);
    }
    List<PoolImpl> pools = listAllByPlatformAndSearch(platformType, barcode);
    return pools.size() == 1 ? pools.get(0) : null;
  }

  @SuppressWarnings("rawtypes")
  public Pool getPoolByExperiment(Experiment e) {
    Pool rtn = null;
    if (e.getPlatform() != null) {
      final Criteria criteria = getCriteriaForClass(Pool.class);

      switch (e.getPlatform().getPlatformType()) {
      case ILLUMINA:
        criteria.add(Restrictions.eq("platformType", PlatformType.ILLUMINA));
        break;
      case IONTORRENT:
        criteria.add(Restrictions.eq("platformType", PlatformType.IONTORRENT));
        break;
      case LS454:
        criteria.add(Restrictions.eq("platformType", PlatformType.LS454));
        break;
      case OXFORDNANOPORE:
        criteria.add(Restrictions.eq("platformType", PlatformType.OXFORDNANOPORE));
        break;
      case PACBIO:
        criteria.add(Restrictions.eq("platformType", PlatformType.PACBIO));
        break;
      case SOLID:
        criteria.add(Restrictions.eq("platformType", PlatformType.SOLID));
        break;
      default:
        break;
      }
      criteria.add(Restrictions
          .sqlRestriction("poolId IN (SELECT pool_poolId FROM Pool_Experiment WHERE experiments_experimentId=" + e.getId() + ")"));
      rtn = (Pool) criteria.uniqueResult();
    }
    return rtn;
  }

  @CoverageIgnore
  public HibernatePoolChangeLogDao getPoolChangeLogDAO() {
    return poolChangeLogDAO;
  }

  @SuppressWarnings("unchecked")
  public Collection<Pool> getPools() {
    final Criteria criteria = getCriteriaForClass(Pool.class);
    return criteria.list();

  }

  @CoverageIgnore
  public Store<SecurityProfile> getSecurityProfileDAO() {
    return securityProfileDAO;
  }

  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  public SQLPoolDAO getSqlPoolDAO() {
    return sqlPoolDAO;
  }

  @CoverageIgnore
  public WatcherStore getWatcherDAO() {
    return watcherDAO;
  }

  @CoverageIgnore
  @SuppressWarnings("rawtypes")
  public PoolImpl lazyGet(long poolId) throws IOException {
    return get(poolId);
  }

  @SuppressWarnings("unchecked")
  public List<PoolImpl> listAll() throws IOException {
    return getCriteriaForClass(PoolImpl.class).list();
  }

  @SuppressWarnings("unchecked")
  public List<Pool<? extends Poolable<?, ?>>> listAllByPlatform(PlatformType platformType) throws IOException {
    if (platformType == null) {
      throw new NullPointerException("Must supply a platform type.");
    }
    return getCriteriaForClass(PoolImpl.class).add(Restrictions.eq("platformType", platformType)).list();
  }

  @SuppressWarnings("unchecked")
  public List<PoolImpl> listAllByPlatformAndSearch(PlatformType platformType, String search) throws IOException {
    if (platformType == null) {
      throw new NullPointerException("Null platformType");
    }
    final Criteria criteria = getCriteriaForClass(PoolImpl.class);
    criteria.add(Restrictions.eq("platformType", platformType));
    if (search != null) {
      search = "%" + search + "%";

      criteria.add(Restrictions.disjunction().add(Restrictions.ilike("name", search)).add(Restrictions.ilike("alias", search))
          .add(Restrictions.ilike("identificationBarcode", search)).add(Restrictions.ilike("description", search)));
    }
    return criteria.list();
  }

  public List<PoolImpl> listByLibraryId(long libraryId) throws IOException {
    return listByRelated(POOL_ID_SELECT_BY_RELATED_LIBRARY, libraryId);
  }

  @SuppressWarnings("unchecked")
  public List<PoolImpl> listByProjectId(long projectId) throws IOException {
    List<PoolImpl> lpools = getCurrentSession().createSQLQuery(DILUTION_POOL_SELECT_BY_RELATED_PROJECT)

        .setBigInteger(0, BigInteger.valueOf(projectId)).list();

    List<PoolImpl> epools = getCurrentSession().createSQLQuery(EMPCR_POOL_SELECT_BY_RELATED_PROJECT)
        .setLong(0, projectId).list();

    List<PoolImpl> ppools = getCurrentSession().createSQLQuery(PLATE_POOL_SELECT_BY_RELATED_PROJECT)
        .setLong(0, projectId).list();

    lpools.addAll(epools);
    lpools.addAll(ppools);
    return lpools;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private List<PoolImpl> listByRelated(String query, long relatedId) throws IOException {
    final SQLQuery sql = getCurrentSession().createSQLQuery(query);
    sql.setLong(0, relatedId);
    List<BigInteger> results = sql.list();
    List<Long> poolIds = new ArrayList<Long>();
    for(BigInteger binteger: results){
      poolIds.add(binteger.longValue());
    }
    return getCriteriaForClass(PoolImpl.class).add(Restrictions.in("poolId", poolIds)).list();
  }

  public List<PoolImpl> listBySampleId(long sampleId) throws IOException {
    return listByRelated(POOL_ID_SELECT_BY_RELATED_SAMPLE, sampleId);
  }

  @SuppressWarnings("unchecked")
  public List<PoolImpl> listReadyByPlatform(PlatformType platformType) {
    if (platformType == null) {
      throw new NullPointerException("Must supply a platform type.");
    }
    return getCriteriaForClass(PoolImpl.class).add(Restrictions.eq("platformType", platformType)).add(Restrictions.eq("readyToRun", true))
        .list();
  }

  @SuppressWarnings("unchecked")
  public long save(final PoolImpl pool) throws IOException, MisoNamingException {
    Long securityProfileId = pool.getSecurityProfile().getProfileId();

    if (securityProfileId == null) {
      securityProfileId = securityProfileDAO.save(pool.getSecurityProfile());
    }
    if (pool.isEmpty()) {
      boxDAO.removeBoxableFromBox(pool);
      pool.setVolume(0D);
    }
    namingScheme.validateField("name", pool.getName());
    if (autoGenerateIdentificationBarcodes) {
      autoGenerateIdBarcode(pool);
    }

    // dwe previously deleted experiments associated with the new pool. For some reason.
    /* poolNamedTemplate.update(POOL_EXPERIMENT_DELETE_BY_POOL_ID, poolparams); */

    watcherDAO.removeWatchedEntityByUser(pool, pool.getLastModifier());

    for (Object u : pool.getWatchers()) {
      watcherDAO.saveWatchedEntityUser(pool, (User) u);
    }

    if (!pool.getNotes().isEmpty()) {
      // TODO Replace collections with Lists and Sets!
      final HashSet<Note> notes = (HashSet<Note>) pool.getNotes();
      for (Note n : notes) {
        noteDAO.savePoolNote(pool, n);
      }
    }
    getCurrentSession().flush();
    final PoolChangeLog changeLog = new PoolChangeLog();
    final StringBuilder message = new StringBuilder(pool.getLastModifier().getLoginName());

    final Map<String, Map<String, Object>> changes = interceptor.getDifferences();
    if (!changes.isEmpty()) {
      final StringBuilder columnsChanged = new StringBuilder();
      message.append(" changed ");
      for (Entry<String, Map<String, Object>> entry : changes.entrySet()) {
        columnsChanged.append(entry.getKey()).append(":");
        message.append(entry.getKey()).append(" from ").append(entry.getValue().get("previous")).append(" to ")
            .append(entry.getValue().get("current")).append(", ");
      }

      changeLog.setPool(pool);
      changeLog.setUser(pool.getLastModifier());
      changeLog.setMessage(message.toString());
      poolChangeLogDAO.save(changeLog);
      System.out.println(message.toString());
    }
    getCurrentSession().saveOrUpdate(pool);

    return pool.getId();

  }

  public long count() {
    return (long) getCriteriaForClass(PoolImpl.class).setProjection(Projections.rowCount()).uniqueResult();
  }

  public long countPoolsByPlatform(PlatformType platform) throws IOException {
    return (long) getCriteriaForClass(PoolImpl.class).setProjection(Projections.rowCount()).add(Restrictions.eq("platformType", platform))
        .uniqueResult();
  }

  public long countPoolsBySearch(PlatformType platform, String queryStr) throws IOException {
    if (isStringEmptyOrNull(queryStr)) {
      return (PlatformType.ILLUMINA.equals(platform) ? countPoolsByPlatform(platform) : count());
    } else {
      queryStr = "%" + queryStr + "%";
      return (long) getCriteriaForClass(PoolImpl.class).setProjection(Projections.rowCount()).add(Restrictions.eq("platformType", platform))
          .add(Restrictions.disjunction().add(Restrictions.ilike("name", queryStr)).add(Restrictions.ilike("alias", queryStr))
              .add(Restrictions.ilike("identificationBarcode", queryStr)).add(Restrictions.ilike("description", queryStr)))
          .uniqueResult();
    }
  }

  public Map<String, Integer> getPoolColumnSizes() throws IOException {
    final Map<String, Integer> rtn = new HashMap<String, Integer>();
    rtn.put("description", AbstractPool.DESCRIPTION_LENGTH);
    rtn.put("concentration", AbstractPool.CONCENTRATION_LENGTH);
    rtn.put("volume", AbstractBoxable.VOLUME_LENGTH);
    rtn.put("name", AbstractPool.NAME_LENGTH);
    rtn.put("alias", AbstractBoxable.ALIAS_LENGTH);

    return rtn;
  }

  public String updateSortCol(String sortCol) {
    sortCol = sortCol.replaceAll("[^\\w]", "");
    if ("id".equals(sortCol)) sortCol = "poolId";
    return sortCol;
  }

  @SuppressWarnings("unchecked")
  public List<PoolImpl> listByOffsetAndNumResults(int offset, int limit, String sortDir, String sortCol, PlatformType platform)
      throws IOException {
    sortCol = updateSortCol(sortCol);
    if (offset < 0 || limit < 0) {
      throw new IOException("Limit and Offset must be greater than zero");
    }
    final Criteria criteria = getCriteriaForClass(PoolImpl.class);
    if ("asc".equalsIgnoreCase(sortDir)) {
      criteria.addOrder(Order.asc(sortCol));
    } else if ("desc".equalsIgnoreCase(sortDir)) {
      criteria.addOrder(Order.desc(sortCol));
    }

    criteria.add(Restrictions.eq("platformType", platform));
    criteria.setFirstResult(offset);
    criteria.setMaxResults(limit);
    return criteria.list();
  }

  @SuppressWarnings("unchecked")
  public List<PoolImpl> listBySearchOffsetAndNumResultsAndPlatform(int offset, int resultsPerPage, String search, String sortDir,
      String sortCol, PlatformType platform) throws IOException {
    if (isStringEmptyOrNull(search)) {
      return listByOffsetAndNumResults(offset, resultsPerPage, sortDir, sortCol, platform);
    } else {
      sortCol = updateSortCol(sortCol);
      if (offset < 0 || resultsPerPage < 0) {
        throw new IOException("Limit and Offset must be greater than zero");
      }

      final Criteria criteria = getCriteriaForClass(PoolImpl.class);
      if ("asc".equalsIgnoreCase(sortDir)) {
        criteria.addOrder(Order.asc(sortCol));
      } else if ("desc".equalsIgnoreCase(sortDir)) {
        criteria.addOrder(Order.desc(sortCol));
      }
      criteria.add(Restrictions.eq("platformType", platform));
      criteria.setFirstResult(offset);
      criteria.setMaxResults(resultsPerPage);
      final String querystr = DbUtils.convertStringToSearchQuery(search);

      criteria.add(Restrictions.eq("platformType", platform));
      criteria.add(
          Restrictions.disjunction().add(Restrictions.ilike("name", querystr)).add(Restrictions.ilike("identificationBarcode", querystr))
              .add(Restrictions.ilike("alias", querystr)).add(Restrictions.ilike("description", querystr)));
      return criteria.list();
    }
  }

  @SuppressWarnings("unchecked")
  public List<PoolImpl> listAllPoolsWithLimit(int limit) throws IOException {
    List<PoolImpl> rtn;
    if (limit == 0) {
      rtn = new ArrayList<PoolImpl>();
    } else {
      rtn = getCriteriaForClass(PoolImpl.class).setMaxResults(limit).list();
    }
    return rtn;
  }

  @SuppressWarnings("unchecked")
  @Deprecated
  @CoverageIgnore
  public List<PoolImpl> listBySearch(String query) {
    List<PoolImpl> rtn;
    if (isStringEmptyOrNull(query)) {
      rtn = new ArrayList<>();
    } else {
      String querystr = DbUtils.convertStringToSearchQuery(query);
      final Criteria criteria = getCriteriaForClass(PoolImpl.class);
      criteria.add(Restrictions.disjunction().add(Restrictions.ilike("name", querystr)).add(Restrictions.ilike("alias", querystr))
          .add(Restrictions.ilike("description", querystr)));
      rtn = criteria.list();
    }
    return rtn;
  }

  @CoverageIgnore
  public void setAutoGenerateIdentificationBarcodes(boolean autoGenerateIdentificationBarcodes) {
    this.autoGenerateIdentificationBarcodes = autoGenerateIdentificationBarcodes;
  }

  @CoverageIgnore
  public void setBoxDAO(BoxStore boxDAO) {
    this.boxDAO = boxDAO;
  }

  @CoverageIgnore
  public void setNamingScheme(MisoNamingScheme<Pool<? extends Poolable<?, ?>>> namingScheme) {
    this.namingScheme = namingScheme;
  }

  @CoverageIgnore
  public void setNoteDAO(NoteStore noteDAO) {
    this.noteDAO = noteDAO;
  }

  @CoverageIgnore
  public void setPoolChangeLogDAO(HibernatePoolChangeLogDao poolChangeLogDAO) {
    this.poolChangeLogDAO = poolChangeLogDAO;
  }

  @CoverageIgnore
  public void setSecurityProfileDAO(Store<SecurityProfile> securityProfileDAO) {
    this.securityProfileDAO = securityProfileDAO;
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public void setSqlPoolDAO(SQLPoolDAO sqlPoolDAO) {
    this.sqlPoolDAO = sqlPoolDAO;
  }

  @CoverageIgnore
  public void setWatcherDAO(WatcherStore watcherDAO) {
    this.watcherDAO = watcherDAO;
  }
}
