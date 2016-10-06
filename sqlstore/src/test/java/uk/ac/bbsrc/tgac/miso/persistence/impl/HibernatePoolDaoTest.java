package uk.ac.bbsrc.tgac.miso.persistence.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import com.eaglegenomics.simlims.core.Note;
import com.eaglegenomics.simlims.core.SecurityProfile;
import com.eaglegenomics.simlims.core.User;

import uk.ac.bbsrc.tgac.miso.AbstractDAOTest;
import uk.ac.bbsrc.tgac.miso.core.data.Boxable;
import uk.ac.bbsrc.tgac.miso.core.data.Experiment;
import uk.ac.bbsrc.tgac.miso.core.data.Platform;
import uk.ac.bbsrc.tgac.miso.core.data.Pool;
import uk.ac.bbsrc.tgac.miso.core.data.Poolable;
import uk.ac.bbsrc.tgac.miso.core.data.impl.ExperimentImpl;
import uk.ac.bbsrc.tgac.miso.core.data.impl.LibraryDilution;
import uk.ac.bbsrc.tgac.miso.core.data.impl.PlatformImpl;
import uk.ac.bbsrc.tgac.miso.core.data.impl.PoolImpl;
import uk.ac.bbsrc.tgac.miso.core.data.type.PlatformType;
import uk.ac.bbsrc.tgac.miso.core.service.naming.DefaultEntityNamingScheme;
import uk.ac.bbsrc.tgac.miso.core.store.BoxStore;
import uk.ac.bbsrc.tgac.miso.core.store.NoteStore;
import uk.ac.bbsrc.tgac.miso.sqlstore.SQLChangeLogDAO;
import uk.ac.bbsrc.tgac.miso.sqlstore.SQLExperimentDAO;
import uk.ac.bbsrc.tgac.miso.sqlstore.SQLWatcherDAO;

@SuppressWarnings("unchecked")
public class HibernatePoolDaoTest extends AbstractDAOTest {

  private static void compareFields(Pool<? extends Poolable<?, ?>> expected, Pool<? extends Poolable<?, ?>> actual) {
    assertEquals(expected.getConcentration(), actual.getConcentration());
    assertEquals(expected.getName(), actual.getName());
    assertEquals(expected.getIdentificationBarcode(), actual.getIdentificationBarcode());
    assertEquals(expected.getCreationDate(), actual.getCreationDate());
    assertEquals(expected.getSecurityProfile().getProfileId(), actual.getSecurityProfile().getProfileId());
    final List<Experiment> expectedExperiments = (List<Experiment>) expected.getExperiments();
    final List<Experiment> actualExperiments = (List<Experiment>) actual.getExperiments();
    assertEquals(expectedExperiments.size(), actualExperiments.size());
    for (int i = 0; i < expectedExperiments.size(); i++) {
      assertEquals(expectedExperiments.get(i).getId(), actualExperiments.get(i).getId());
    }
    assertEquals(expected.getPlatformType(), actual.getPlatformType());
    assertEquals(expected.getReadyToRun(), actual.getReadyToRun());
    assertEquals(expected.getAlias(), actual.getAlias());
    assertEquals(expected.getLastModifier().getUserId(), actual.getLastModifier().getUserId());
    assertEquals(expected.getBoxPositionId(), actual.getBoxPositionId());
    assertEquals(expected.isEmpty(), actual.isEmpty());
    if (!expected.isEmpty()) {
      assertEquals(expected.getVolume(), actual.getVolume());
    } else {
      assertEquals(new Double(0D), actual.getVolume());
    }
    assertEquals(expected.getQcPassed(), actual.getQcPassed());
    assertEquals(expected.getDescription(), actual.getDescription());
    assertEquals(expected.getNotes().size(), actual.getNotes().size());
  }

  @Mock
  final BoxStore boxDao = Mockito.mock(BoxStore.class);

  @Rule
  public final ExpectedException exception = ExpectedException.none();
  @SuppressWarnings("rawtypes")
  @Mock
  final DefaultEntityNamingScheme namingScheme = Mockito.mock(DefaultEntityNamingScheme.class);

  @Mock
  final NoteStore noteDao = Mockito.mock(NoteStore.class);

  @Autowired
  private SessionFactory sessionFactory;

  @Mock
  final private SQLWatcherDAO watcherDao = Mockito.mock(SQLWatcherDAO.class);

  @Mock
  private final SQLChangeLogDAO sqlChangeLogDAO = Mockito.mock(SQLChangeLogDAO.class);

  @Mock
  private final SQLExperimentDAO sqlExperimentDAO = Mockito.mock(SQLExperimentDAO.class);

  @InjectMocks
  private HibernatePoolDao dao;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    dao.setSessionFactory(sessionFactory);
    dao.setSqlExperimentDAO(sqlExperimentDAO);
  }

  // TODO: Write migration to turn all PlatformType into uppercase

  @Test
  public void testGetByBarcode() throws IOException {
    assertNotNull(dao.getByBarcode("IPO3::Illumina"));
  }

  @Test
  public void testGetByBarcodeNull() throws IOException {
    exception.expect(NullPointerException.class);
    dao.getByBarcode(null);
  }

  @Test
  public void testGetByBarcodeList() throws IOException {
    final List<String> barcodes = new ArrayList<>();
    barcodes.add("IPO2::Illumina");
    barcodes.add("IPO3::Illumina");
    assertEquals(2, dao.getByBarcodeList(barcodes).size());
  }

  @Test
  public void testGetByBarcodeListEmpty() {
    assertEquals(0, dao.getByBarcodeList(new ArrayList<String>()).size());
  }

  @Test
  public void testGetByBarcodeListNone() throws IOException {
    final List<String> barcodes = new ArrayList<>();
    barcodes.add("asdf");
    barcodes.add("jkl;");
    assertEquals(0, dao.getByBarcodeList(barcodes).size());
  }

  @Test
  public void testGetByBarcodeListNull() {
    exception.expect(NullPointerException.class);
    dao.getByBarcodeList(null);
  }

  @Test
  public void testGetByPositionId() throws IOException {
    assertNotNull(dao.getByPositionId(201L));
  }

  @Test
  public void testGetByPositionIdNone() throws IOException {
    assertNull(dao.getByPositionId(9999L));
  }

  @Test
  public void testGetPoolByBarcode() throws IOException {
    assertNotNull(dao.getPoolByBarcode("IPO3::Illumina", PlatformType.ILLUMINA));
  }

  @Test
  public void testGetPoolByBarcodeNull() throws IOException {
    exception.expect(NullPointerException.class);
    dao.getPoolByBarcode(null, PlatformType.ILLUMINA);
  }

  @Test
  public void testGetPoolByBarcodeNullPlatform() throws IOException {
    assertNull(dao.getPoolByBarcode("", null));
  }

  @Test
  public void testGetPoolByBarcodeNullBarcode() throws Exception {
    exception.expect(NullPointerException.class);
    dao.getPoolByBarcode(null, PlatformType.ILLUMINA);
  }

  @Test
  public void testGetPoolByExperiment() throws Exception {
    final Experiment mockExperiment = Mockito.mock(Experiment.class);
    final Platform mockPlatform = Mockito.mock(Platform.class);
    Mockito.when(mockExperiment.getPlatform()).thenReturn(mockPlatform);
    Mockito.when(mockPlatform.getPlatformType()).thenReturn(PlatformType.ILLUMINA);
    Mockito.when(mockExperiment.getId()).thenReturn(1L);
    final Pool<? extends Poolable<?, ?>> result = dao.getPoolByExperiment(mockExperiment);
    assertNotNull(result);
    assertEquals(PlatformType.ILLUMINA, result.getPlatformType());
    assertEquals(1L, result.getId());
    assertEquals(2D, result.getConcentration(), 0D);
    assertEquals("IPO1::Illumina", result.getIdentificationBarcode());
    assertEquals("IPO1", result.getName());
    assertEquals("TEST", result.getDescription());
    assertEquals("2015-08-27 00:00:00.0", result.getCreationDate().toString());
    assertEquals(Long.valueOf(2), result.getSecurityProfile().getProfileId());
    // experiment_experimentId - TODO: pool and experiment have a many to many relationship.
    // perhaps this field can be removed.
    assertEquals(true, result.getReadyToRun());
    assertEquals("Pool 1", result.getAlias());
    assertNull(result.getQcPassed());
    assertEquals(Long.valueOf(201), result.getBoxPositionId());
  }

  @Test
  public void testGetPoolByNullExperiment() throws Exception {
    exception.expect(NullPointerException.class);
    dao.getPoolByExperiment(null);
  }

  @Test
  public void testGetPools() throws Exception {
    final Collection<Pool<? extends Poolable<?, ?>>> pools = dao.getPools();
    assertNotNull(pools);
    // TODO: a bit more detail.
    // also, this number keeps changing, investigate
  }

  @Test
  public void testListAll() throws IOException {
    assertEquals(10, dao.listAll().size());
  }

  @Test
  public void testListAllByPlatformAndSearchByName() throws IOException {
    assertEquals(1, dao.listAllByPlatformAndSearch(PlatformType.ILLUMINA, "IPO5").size());
  }

  @Test
  public void testListAllByPlatformAndSearchByAlias() throws IOException {
    assertEquals(1, dao.listAllByPlatformAndSearch(PlatformType.ILLUMINA, "Pool 5").size());
  }

  @Test
  public void testListAllByPlatformAndSearchByIdentificationBarcode() throws IOException {
    assertEquals(1, dao.listAllByPlatformAndSearch(PlatformType.ILLUMINA, "IPO5::Illumina").size());
  }

  @Test
  public void testListByLibraryId() throws IOException {
    assertEquals(2, dao.listByLibraryId(1L).size());
  }

  @Test
  public void testListByLibraryIdNone() throws IOException {
    assertEquals(0, dao.listByLibraryId(100L).size());
  }

  @Test
  public void testListByProjectId() throws IOException {
    assertEquals(10, dao.listByProjectId(1L).size());
  }

  @Test
  public void testListByProjectNone() throws IOException {
    assertEquals(0, dao.listByProjectId(100L).size());
  }

  @Test
  public void testListBySampleId() throws IOException {
    assertEquals(2, dao.listBySampleId(2L).size());
  }

  @Test
  public void testListBySampleIdNone() throws IOException {
    assertEquals(0, dao.listBySampleId(100L).size());
  }

  @Ignore // TODO: fix me
  @Test
  public void testListReadyByPlatform() throws IOException {
    assertEquals(5, dao.listReadyByPlatform(PlatformType.ILLUMINA).size());
  }

  @Test
  public void testListReadyByPlatformNone() throws IOException {
    assertEquals(0, dao.listReadyByPlatform(PlatformType.SOLID).size());
  }

  @Test
  public void testListReadyByPlatformNull() throws IOException {
    exception.expect(NullPointerException.class);
    dao.listReadyByPlatform(null).size();
  }

  private static Pool<? extends Poolable<?, ?>> getATestPool(int counter, Date creationDate, boolean empty, int notes) {
    final Pool<? extends Poolable<?, ?>> rtn = new PoolImpl<>();
    final Experiment mockExperiment = Mockito.mock(Experiment.class);
    final SecurityProfile mockSecurityProfile = Mockito.mock(SecurityProfile.class);
    final User mockUser = Mockito.mock(User.class);

    Mockito.when(mockExperiment.getId()).thenReturn(1L);
    Mockito.when(mockSecurityProfile.getProfileId()).thenReturn(1L);
    Mockito.when(mockUser.getUserId()).thenReturn(1L);
    Mockito.when(mockUser.getLoginName()).thenReturn("franklin");

    rtn.setConcentration((double) counter);
    rtn.setName("Test Pool " + counter);
    rtn.setIdentificationBarcode("BOOP" + counter);
    rtn.setCreationDate(creationDate);
    rtn.setSecurityProfile(mockSecurityProfile);
    rtn.setExperiments(Arrays.asList(mockExperiment));
    rtn.setPlatformType(PlatformType.ILLUMINA);
    rtn.setReadyToRun(true);
    rtn.setAlias("Alias " + counter);
    rtn.setLastModifier(mockUser);
    rtn.setBoxPositionId(1L);
    rtn.setEmpty(empty);
    rtn.setVolume(new Double(counter));
    rtn.setQcPassed(false);
    rtn.setDescription("Description " + counter);
    if (notes > 0) {
      for (int i = 0; i < notes; i++) {
        final Note note = Mockito.mock(Note.class);
        rtn.addNote(note);
      }
    }

    return rtn;
  }

  @Test
  public void testAutoGenerateIdBarcode() {
    final Pool<LibraryDilution> p = new PoolImpl<>();
    p.setName("name");
    p.setPlatformType(PlatformType.ILLUMINA);
    dao.autoGenerateIdBarcode(p);
    assertEquals("name::" + PlatformType.ILLUMINA.getKey(), p.getIdentificationBarcode());
  }

  @Test
  public void testCount() throws IOException {
    assertEquals(10L, dao.count());
  }

  @Test
  public void testCountIlluminaPools() throws IOException {
    assertEquals(10L, dao.countPoolsByPlatform(PlatformType.ILLUMINA));
  }

  @Test
  public void testCountIlluminaPoolsBadSearch() throws IOException {
    assertEquals(0L, dao.countPoolsBySearch(PlatformType.ILLUMINA, "; DROP TABLE Pool;"));
  }

  @Test
  public void testCountIlluminaPoolsBySearch() throws IOException {
    assertEquals(2L, dao.countPoolsBySearch(PlatformType.ILLUMINA, "IPO1"));
  }

  @Test
  public void testCountIlluminaPoolsEmptySearch() throws IOException {
    assertEquals(10L, dao.countPoolsBySearch(PlatformType.ILLUMINA, ""));
  }

  @Test
  public void testCountPacBioPools() throws IOException {
    assertEquals(0L, dao.countPoolsByPlatform(PlatformType.PACBIO));
  }

  @Test
  public void testCountPacBioPoolsBySearch() throws IOException {
    assertEquals(0L, dao.countPoolsBySearch(PlatformType.PACBIO, "IPO1"));
  }

  @Test
  public void testGetByBarcodeNone() throws IOException {
    assertNull(dao.getByBarcode("asdf"));
  }

  @Test
  public void testGetNone() throws IOException {
    assertNull(dao.get(100L));
  }

  @Test
  public void testGetPoolByBarcodeNone() throws IOException {
    assertNull(dao.getPoolByBarcode("asdf", PlatformType.ILLUMINA));
  }

  @Test
  public void testGetPoolByExperimentNoneForExperiment() {
    final Experiment exp = new ExperimentImpl();
    exp.setId(100L);
    final Platform plat = new PlatformImpl();
    plat.setPlatformType(PlatformType.ILLUMINA);
    exp.setPlatform(plat);
    assertNull(dao.getPoolByExperiment(exp));
  }

  @Test
  public void testGetPoolByExperimentNoneForPlatform() {
    final Experiment exp = new ExperimentImpl();
    exp.setId(1L);
    final Platform plat = new PlatformImpl();
    plat.setPlatformType(PlatformType.SOLID);
    exp.setPlatform(plat);
    assertNull(dao.getPoolByExperiment(exp));
  }

  @Test
  public void testGet() throws IOException {
    final Pool<?> pool = dao.get(1L);
    assertNotNull(pool);
    assertEquals(1L, pool.getId());
  }

  @Test
  public void testGetPoolByExperimentNullPlatform() throws IOException {
    final Experiment exp = new ExperimentImpl();
    assertNull(dao.getPoolByExperiment(exp));
  }

  @Test
  public void testGetPoolColumnSizes() throws IOException {
    final Map<String, Integer> map = dao.getPoolColumnSizes();
    assertNotNull(map);
    assertFalse(map.isEmpty());
  }

  @Test
  public void testLazyGet() throws IOException {
    final Pool<?> pool = dao.lazyGet(1L);
    assertNotNull(pool);
    assertEquals(1L, pool.getId());
  }

  @Test
  public void testLazyGetNone() throws IOException {
    assertNull(dao.lazyGet(100L));
  }

  @Test
  public void testListAllByPlatformAndSearchNoneForPlatformType() throws IOException {
    assertEquals(0, dao.listAllByPlatformAndSearch(PlatformType.SOLID, "").size());
  }

  @Test
  public void testListAllByPlatformAndSearchNoneForQuery() throws IOException {
    assertEquals(0, dao.listAllByPlatformAndSearch(PlatformType.ILLUMINA, "asdf").size());
  }

  @Test
  public void testListAllByPlatformAndSearchNullPlatformType() throws IOException {
    exception.expect(NullPointerException.class);
    dao.listAllByPlatformAndSearch(null, "");
  }

  @Test
  public void testListAllByPlatformAndSearchNullQuery() throws IOException {
    assertEquals(10, dao.listAllByPlatformAndSearch(PlatformType.ILLUMINA, null).size());
  }

  @Test
  public void testListAllByPlatformAndSearchWithEmptyString() throws IOException {
    assertEquals(10, dao.listAllByPlatformAndSearch(PlatformType.ILLUMINA, "").size());
  }

  @Test
  public void testListAllByPlatformNone() throws IOException {
    assertEquals(0, dao.listAllByPlatform(PlatformType.SOLID).size());
  }

  @Test
  public void testListAllByPlatformNull() throws IOException {
    exception.expect(NullPointerException.class);
    dao.listAllByPlatform(null);
  }

  @Test
  public void testListByIlluminaBadSearchWithLimit() throws IOException {
    final List<Pool<? extends Poolable<?, ?>>> pools = dao.listBySearchOffsetAndNumResultsAndPlatform(5, 3, "; DROP TABLE Pool;", "asc",
        "id", PlatformType.ILLUMINA);
    assertEquals(0L, pools.size());
  }

  @Test
  public void testListByIlluminaEmptySearchWithLimit() throws IOException {
    final List<Pool<? extends Poolable<?, ?>>> pools = dao.listBySearchOffsetAndNumResultsAndPlatform(5, 3, "", "asc", "id",
        PlatformType.ILLUMINA);
    assertEquals(3L, pools.size());
  }

  @Test
  public void testListByIlluminaOffsetBadSortDir() throws IOException {
    final List<Pool<? extends Poolable<?, ?>>> pools = dao.listByOffsetAndNumResults(5, 3, "BARK", "id", PlatformType.ILLUMINA);
    assertEquals(3, pools.size());
  }

  @Test
  public void testListByIlluminaSearchWithLimit() throws IOException {
    final List<Pool<? extends Poolable<?, ?>>> pools = dao.listBySearchOffsetAndNumResultsAndPlatform(5, 3, "IPO", "asc", "id",
        PlatformType.ILLUMINA);
    assertEquals(3, pools.size());
    assertEquals(6L, pools.get(0).getId());
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testListBySearchWithBadQuery() throws IOException {
    assertEquals(0, dao.listBySearch(";DROP TABLE Users;").size());
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testListBySearchWithGoodAliasQuery() throws IOException {
    dao.listAll();
    assertEquals(2, dao.listBySearch("IPO1").size());
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testListBySearchWithGoodNameQuery() throws IOException {
    assertEquals(2, dao.listBySearch("Pool 1").size());
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testListBySearchWithNullQuery() throws IOException {
    assertEquals(0, dao.listBySearch(null).size());
  }

  @Test
  public void testListIlluminaOffsetBadLimit() throws IOException {
    exception.expect(IOException.class);
    dao.listByOffsetAndNumResults(5, -3, "asc", "id", PlatformType.ILLUMINA);
  }

  @Test
  public void testListIlluminaOffsetThreeWithThreeSamplesPerPageOrderLastMod() throws IOException {
    final List<Pool<? extends Poolable<?, ?>>> pools = dao.listByOffsetAndNumResults(3, 3, "desc", "lastModified", PlatformType.ILLUMINA);
    assertEquals(3, pools.size());
    assertEquals(7, pools.get(0).getId());
  }

  @Test
  public void testListIlluminaPoolsWithLimitAndOffset() throws IOException {
    assertEquals(3, dao.listByOffsetAndNumResults(5, 3, "ASC", "id", PlatformType.ILLUMINA).size());
  }

  @Test
  public void testListPoolsWithLimit() throws IOException {
    assertEquals(10, dao.listAllPoolsWithLimit(10).size());
  }

  @Test
  public void testListPoolsWithLimitZero() throws IOException {
    assertEquals(0, dao.listAllPoolsWithLimit(0).size());
  }

  @Test
  public void testLastModified() throws IOException {
    final Pool<?> pool = dao.get(1L);
    assertNotNull(pool);
    assertEquals(1L, pool.getId());
    assertNotNull(pool.getLastModified());
  }

  @SuppressWarnings({})
  @Test
  public void testSaveEmpty() throws Exception {

    final Date creationDate = new Date();
    final Pool<? extends Poolable<?, ?>> saveMe = getATestPool(1, creationDate, true, 0);
    assertEquals(10, dao.count());
    final long rtn = dao.save(saveMe);
    assertEquals(11, dao.count());

    Mockito.verify(boxDao).removeBoxableFromBox(Mockito.any(Boxable.class));
    Mockito.verify(namingScheme).validateField(Mockito.anyString(), Mockito.anyString());
    Mockito.verify(watcherDao).removeWatchedEntityByUser(Mockito.any(Pool.class), Mockito.any(User.class));
    Mockito.verifyZeroInteractions(noteDao);

    final Pool<? extends Poolable<?, ?>> freshPool = dao.get(rtn);
    final Pool<? extends Poolable<?, ?>> comparePool = getATestPool(1, creationDate, true, 0);
    compareFields(comparePool, freshPool);
    dao.remove(saveMe);
    dao.getCurrentSession().flush();
    assertEquals(10, dao.count());

  }

  @SuppressWarnings({})
  @Test
  public void testSaveEmptyWithNotes() throws Exception {

    final Date creationDate = new Date();
    final Pool<? extends Poolable<?, ?>> saveMe = getATestPool(1, creationDate, true, 10);
    final long rtn = dao.save(saveMe);
    Mockito.verifyZeroInteractions(sqlChangeLogDAO);
    Mockito.verify(boxDao).removeBoxableFromBox(Mockito.any(Boxable.class));
    Mockito.verify(namingScheme).validateField(Mockito.anyString(), Mockito.anyString());
    Mockito.verify(watcherDao).removeWatchedEntityByUser(Mockito.any(Pool.class), Mockito.any(User.class));
    Mockito.verify(noteDao, Mockito.times(10)).savePoolNote(Mockito.any(Pool.class), Mockito.any(Note.class));

    final Pool<? extends Poolable<?, ?>> freshPool = dao.get(rtn);
    final Pool<? extends Poolable<?, ?>> comparePool = getATestPool(1, creationDate, true, 10);
    compareFields(comparePool, freshPool);
    dao.remove(saveMe);
    dao.getCurrentSession().flush();

  }

  @SuppressWarnings({})
  @Test
  public void testSaveNonEmpty() throws Exception {

    final Date creationDate = new Date();
    final Pool<? extends Poolable<?, ?>> saveMe = getATestPool(1, creationDate, false, 0);
    final long rtn = dao.save(saveMe);
    Mockito.verifyZeroInteractions(boxDao);
    Mockito.verifyZeroInteractions(sqlChangeLogDAO);
    Mockito.verify(namingScheme).validateField(Mockito.anyString(), Mockito.anyString());
    Mockito.verify(watcherDao).removeWatchedEntityByUser(Mockito.any(Pool.class), Mockito.any(User.class));
    Mockito.verifyZeroInteractions(noteDao);

    // check they're actually the same
    final Pool<? extends Poolable<?, ?>> freshPool = dao.get(rtn);
    final Pool<? extends Poolable<?, ?>> comparePool = getATestPool(1, creationDate, false, 0);
    compareFields(comparePool, freshPool);
    dao.remove(saveMe);
    dao.getCurrentSession().flush();
  }

}
