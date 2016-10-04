package uk.ac.bbsrc.tgac.miso.persistence.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.transaction.annotation.Transactional;

import com.eaglegenomics.simlims.core.Note;
import com.eaglegenomics.simlims.core.SecurityProfile;
import com.eaglegenomics.simlims.core.User;

import uk.ac.bbsrc.tgac.miso.AbstractDAOTest;
import uk.ac.bbsrc.tgac.miso.core.data.Boxable;
import uk.ac.bbsrc.tgac.miso.core.data.Experiment;
import uk.ac.bbsrc.tgac.miso.core.data.Platform;
import uk.ac.bbsrc.tgac.miso.core.data.Pool;
import uk.ac.bbsrc.tgac.miso.core.data.impl.ExperimentImpl;
import uk.ac.bbsrc.tgac.miso.core.data.impl.LibraryDilution;
import uk.ac.bbsrc.tgac.miso.core.data.impl.PlatformImpl;
import uk.ac.bbsrc.tgac.miso.core.data.impl.PoolChangeLog;
import uk.ac.bbsrc.tgac.miso.core.data.impl.PoolImpl;
import uk.ac.bbsrc.tgac.miso.core.data.type.PlatformType;
import uk.ac.bbsrc.tgac.miso.core.service.naming.DefaultEntityNamingScheme;
import uk.ac.bbsrc.tgac.miso.core.store.BoxStore;
import uk.ac.bbsrc.tgac.miso.core.store.NoteStore;
import uk.ac.bbsrc.tgac.miso.core.store.WatcherStore;

@Transactional
public class HibernatePoolDaoTest extends AbstractDAOTest {

  @SuppressWarnings("unchecked")
  private static void compareFields(PoolImpl expected, PoolImpl actual) {
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
  @SuppressWarnings("unchecked")
  private static PoolImpl getATestPool(int counter, Date creationDate, boolean empty, int notes) {
    final PoolImpl rtn = new PoolImpl<>();
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
        Note note = Mockito.mock(Note.class);
        rtn.addNote(note);
      }
    }

    return rtn;
  }
  @Mock
  final BoxStore boxDao = Mockito.mock(BoxStore.class);
  @Mock
  final HibernatePoolChangeLogDao changeLogDao = Mockito.mock(HibernatePoolChangeLogDao.class);
  @InjectMocks
  private HibernatePoolDao dao;
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
  final WatcherStore watcherDao = Mockito.mock(WatcherStore.class);

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    dao.setSessionFactory(sessionFactory);
  }

  // TODO: Write migration to turn all PlatformType into uppercase

  @Test
  public void testChangeLogFunctionality() throws Exception {
    User newModifier = Mockito.mock(User.class);
    PoolImpl testPool = getATestPool(1, new Date(), false, 0);
    Mockito.when(newModifier.getLoginName()).thenReturn("Nick Cage");

    dao.save(testPool);
    // There are no changes
    Mockito.verifyZeroInteractions(changeLogDao);

    testPool.setConcentration(5D);
    testPool.setName("Test Pool xxx");
    testPool.setIdentificationBarcode("Foob");
    testPool.setCreationDate(new Date());
    testPool.setSecurityProfile(Mockito.mock(SecurityProfile.class));
    testPool.setExperiments(new ArrayList<>());
    testPool.setPlatformType(PlatformType.IONTORRENT);
    testPool.setReadyToRun(false);
    testPool.setAlias("Alias changed");
    testPool.setLastModifier(newModifier);
    testPool.setBoxPositionId(2L);
    testPool.setEmpty(true);
    testPool.setVolume(10D);
    testPool.setQcPassed(true);
    testPool.setDescription("Description changed");
    dao.save(testPool);
    // there are changes
    Mockito.verify(changeLogDao).save(Mockito.any(PoolChangeLog.class));
    // TODO: a more in depth test in HibernateChangeLogDaoTest
  }

  @Test
  public void testGetByBarcode() throws Exception {
    final PoolImpl testPool = getATestPool(17, new Date(), false, 3);
    final String idBarcode = testPool.getIdentificationBarcode();
    // non existing pool check
    assertNull(dao.getByBarcode(idBarcode));
    dao.save(testPool);
    PoolImpl result = dao.getByBarcode(idBarcode);
    assertNotNull(result);
    compareFields(testPool, result);
  }

  @Test
  public void testGetByBarcodeList() throws IOException {
    List<String> barcodes = new ArrayList<>();
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
    List<String> barcodes = new ArrayList<>();
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
  public void testGetByBarcodeManyWithSameBarcode() throws Exception {
    // This returns the first one it finds, by design.
    final String idBarcode = "beep";
    final PoolImpl testPool1 = getATestPool(18, new Date(), false, 3);
    final PoolImpl testPool2 = getATestPool(19, new Date(), true, 2);
    final PoolImpl testPool3 = getATestPool(20, new Date(), false, 1);
    final PoolImpl[] testPools = { testPool1, testPool2, testPool3 };

    for (PoolImpl pool : testPools) {
      pool.setIdentificationBarcode(idBarcode);
      dao.save(pool);
    }

    PoolImpl result = dao.getByBarcode(idBarcode);
    assertNotNull(result);
  }

  @Test
  public void testGetByBarcodeNull() throws Exception {
    final PoolImpl testPool = getATestPool(17, new Date(), false, 3);
    exception.expect(NullPointerException.class);
    dao.getByBarcode(null);
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
  public void testGetPoolByBarcode() throws Exception {
    final PoolImpl testPool = getATestPool(12, new Date(), false, 3);
    final String idBarcode = testPool.getIdentificationBarcode();
    // non existing pool check
    assertNull(dao.getPoolByBarcode(idBarcode, PlatformType.ILLUMINA));
    dao.save(testPool);
    PoolImpl result = dao.getPoolByBarcode(idBarcode, PlatformType.ILLUMINA);
    assertNotNull(result);
    compareFields(testPool, result);
  }

  @Test
  public void testGetPoolByBarcodeManyWithSameBarcode() throws Exception {
    // This returns null, by design.
    final String idBarcode = "beep";
    final PoolImpl testPool1 = getATestPool(14, new Date(), false, 3);
    final PoolImpl testPool2 = getATestPool(15, new Date(), true, 2);
    final PoolImpl testPool3 = getATestPool(16, new Date(), false, 1);
    final PoolImpl[] testPools = { testPool1, testPool2, testPool3 };

    for (PoolImpl pool : testPools) {
      pool.setIdentificationBarcode(idBarcode);
      dao.save(pool);
    }

    PoolImpl result = dao.getPoolByBarcode(idBarcode, PlatformType.ILLUMINA);
    assertNull(result);
  }

  @Test
  public void testGetPoolByBarcodeNullBarcode() throws Exception {
    exception.expect(NullPointerException.class);
    dao.getPoolByBarcode(null, PlatformType.ILLUMINA);
  }

  @Test
  public void testGetPoolByBarcodeNullPlatformType() throws Exception {
    final PoolImpl testPool = getATestPool(13, new Date(), false, 3);
    final String idBarcode = testPool.getIdentificationBarcode();
    assertNull(dao.getPoolByBarcode(idBarcode, null));
    dao.save(testPool);
    PoolImpl result = dao.getPoolByBarcode(idBarcode, null);
    assertNotNull(result);
    compareFields(testPool, result);
  }

  @Test
  public void testGetPoolByExperiment() throws Exception {
    final Experiment mockExperiment = Mockito.mock(Experiment.class);
    final Platform mockPlatform = Mockito.mock(Platform.class);
    Mockito.when(mockExperiment.getPlatform()).thenReturn(mockPlatform);
    Mockito.when(mockPlatform.getPlatformType()).thenReturn(PlatformType.ILLUMINA);
    Mockito.when(mockExperiment.getId()).thenReturn(1L);
    Pool result = dao.getPoolByExperiment(mockExperiment);
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
    List<Pool> pools = (List<Pool>) dao.getPools();
    assertNotNull(pools);
    // TODO: a bit more detail.
    // also, this number keeps changing, investigate
  }

  @Ignore
  //TODO: the count is all messed up because we inserted things into the database and there
  // is no rollback :(
  @Test
  public void testListAll() throws IOException {
    assertEquals(10, dao.listAll().size());
  }

  @Ignore //TODO: fixme.
  @Test
  public void testListAllByPlatform() throws IOException {
    assertEquals(10, dao.listAllByPlatform(PlatformType.ILLUMINA).size());
  }

  @Test
  public void testListAllByPlatformAndSearch_alias() throws Exception {
    // search on name, alias, identificationBarcode and description
    PoolImpl test1 = getATestPool(81, new Date(), false, 3);
    PoolImpl test2 = getATestPool(82, new Date(), true, 2);
    PoolImpl test3 = getATestPool(83, new Date(), false, 1);

    dao.save(test1);
    dao.save(test2);
    dao.save(test3);
    assertEquals(3, dao.listAllByPlatformAndSearch(PlatformType.ILLUMINA, "alias 8").size());
    assertEquals(1, dao.listAllByPlatformAndSearch(PlatformType.ILLUMINA, "alias 81").size());
  }

  @Test
  public void testListAllByPlatformAndSearch_description() throws Exception {
    // search on name, alias, identificationBarcode and description
    PoolImpl test1 = getATestPool(61, new Date(), false, 3);
    PoolImpl test2 = getATestPool(62, new Date(), true, 2);
    PoolImpl test3 = getATestPool(63, new Date(), false, 1);

    dao.save(test1);
    dao.save(test2);
    dao.save(test3);
    assertEquals(3, dao.listAllByPlatformAndSearch(PlatformType.ILLUMINA, "description 6").size());
    assertEquals(1, dao.listAllByPlatformAndSearch(PlatformType.ILLUMINA, "description 61").size());
  }

  @Test
  public void testListAllByPlatformAndSearch_identificationBarcode() throws Exception {
    // search on name, alias, identificationBarcode and description
    PoolImpl test1 = getATestPool(71, new Date(), false, 3);
    PoolImpl test2 = getATestPool(72, new Date(), true, 2);
    PoolImpl test3 = getATestPool(73, new Date(), false, 1);

    dao.save(test1);
    dao.save(test2);
    dao.save(test3);
    assertEquals(3, dao.listAllByPlatformAndSearch(PlatformType.ILLUMINA, "boop7").size());
    assertEquals(1, dao.listAllByPlatformAndSearch(PlatformType.ILLUMINA, "boop71").size());
  }

  @Test
  public void testListAllByPlatformAndSearch_Name() throws Exception {
    // search on name, alias, identificationBarcode and description
    PoolImpl test1 = getATestPool(91, new Date(), false, 3);
    PoolImpl test2 = getATestPool(92, new Date(), true, 2);
    PoolImpl test3 = getATestPool(93, new Date(), false, 1);

    dao.save(test1);
    dao.save(test2);
    dao.save(test3);
    assertEquals(3, dao.listAllByPlatformAndSearch(PlatformType.ILLUMINA, "test pool 9").size());
    assertEquals(1, dao.listAllByPlatformAndSearch(PlatformType.ILLUMINA, "test pool 91").size());
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

  @Ignore //TODO: fix me
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

  @SuppressWarnings({ "rawtypes" })
  @Test
  public void testSaveEmpty() throws Exception {

    final Date creationDate = new Date();
    final PoolImpl saveMe = getATestPool(1, creationDate, true, 0);
    final long rtn = dao.save(saveMe);
    Mockito.verifyZeroInteractions(changeLogDao);
    Mockito.verify(boxDao).removeBoxableFromBox(Mockito.any(Boxable.class));
    Mockito.verify(namingScheme).validateField(Mockito.anyString(), Mockito.anyString());
    Mockito.verify(watcherDao).removeWatchedEntityByUser(Mockito.any(Pool.class), Mockito.any(User.class));
    Mockito.verifyZeroInteractions(noteDao);

    // check they're actually the same
    PoolImpl freshPool = dao.get(rtn);
    PoolImpl comparePool = getATestPool(1, creationDate, true, 0);
    compareFields(comparePool, freshPool);
  }

  @SuppressWarnings({ "rawtypes" })
  @Test
  public void testSaveEmptyWithNotes() throws Exception {

    final Date creationDate = new Date();
    final PoolImpl saveMe = getATestPool(1, creationDate, true, 10);
    final long rtn = dao.save(saveMe);
    Mockito.verifyZeroInteractions(changeLogDao);
    Mockito.verify(boxDao).removeBoxableFromBox(Mockito.any(Boxable.class));
    Mockito.verify(namingScheme).validateField(Mockito.anyString(), Mockito.anyString());
    Mockito.verify(watcherDao).removeWatchedEntityByUser(Mockito.any(Pool.class), Mockito.any(User.class));
    Mockito.verify(noteDao, Mockito.times(10)).savePoolNote(Mockito.any(Pool.class), Mockito.any(Note.class));

    // check they're actually the same
    PoolImpl freshPool = dao.get(rtn);
    PoolImpl comparePool = getATestPool(1, creationDate, true, 10);
    compareFields(comparePool, freshPool);
  }

  @SuppressWarnings({ "rawtypes" })
  @Test
  public void testSaveNonEmpty() throws Exception {

    final Date creationDate = new Date();
    final PoolImpl saveMe = getATestPool(1, creationDate, false, 0);
    final long rtn = dao.save(saveMe);
    Mockito.verifyZeroInteractions(boxDao);
    Mockito.verifyZeroInteractions(changeLogDao);

    Mockito.verify(namingScheme).validateField(Mockito.anyString(), Mockito.anyString());
    Mockito.verify(watcherDao).removeWatchedEntityByUser(Mockito.any(Pool.class), Mockito.any(User.class));
    Mockito.verifyZeroInteractions(noteDao);

    // check they're actually the same
    PoolImpl freshPool = dao.get(rtn);
    PoolImpl comparePool = getATestPool(1, creationDate, false, 0);
    compareFields(comparePool, freshPool);
  }

  @Test
  public void testAutoGenerateIdBarcode() {
    Pool<LibraryDilution> p = new PoolImpl<>();
    p.setName("name");
    p.setPlatformType(PlatformType.ILLUMINA);
    dao.autoGenerateIdBarcode(p);
    assertEquals("name::" + PlatformType.ILLUMINA.getKey(), p.getIdentificationBarcode());
  }

  @Ignore //TODO: //fixme
  @Test
  public void testCount() throws IOException {
    assertEquals(10, dao.count());
  }

  @Ignore //TODO: fix me
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

  @Ignore //TODO: fixme
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
    Experiment exp = new ExperimentImpl();
    exp.setId(100L);
    Platform plat = new PlatformImpl();
    plat.setPlatformType(PlatformType.ILLUMINA);
    exp.setPlatform(plat);
    assertNull(dao.getPoolByExperiment(exp));
  }
  @Test
  public void testGetPoolByExperimentNoneForPlatform() {
    Experiment exp = new ExperimentImpl();
    exp.setId(1L);
    Platform plat = new PlatformImpl();
    plat.setPlatformType(PlatformType.SOLID);
    exp.setPlatform(plat);
    assertNull(dao.getPoolByExperiment(exp));
  }

  @Test
  public void testGet() throws IOException {
    Pool<?> pool = dao.get(1L);
    assertNotNull(pool);
    assertEquals(1L, pool.getId());
  }

  @Test
  public void testGetPoolByExperimentNullPlatform() throws IOException {
    Experiment exp = new ExperimentImpl();
    assertNull(dao.getPoolByExperiment(exp));
  }
  @Test
  public void testGetPoolColumnSizes() throws IOException {
    Map<String, Integer> map = dao.getPoolColumnSizes();
    assertNotNull(map);
    assertFalse(map.isEmpty());
  }

  @Test
  public void testLazyGet() throws IOException {
    Pool<?> pool = dao.lazyGet(1L);
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

  @Ignore //todo: fixme
  @Test
  public void testListAllByPlatformAndSearchNullQuery() throws IOException {
    assertEquals(10, dao.listAllByPlatformAndSearch(PlatformType.ILLUMINA, null).size());
  }

  @Ignore //TODO: fixme
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
    List<PoolImpl> pools = dao
        .listBySearchOffsetAndNumResultsAndPlatform(5, 3, "; DROP TABLE Pool;", "asc", "id", PlatformType.ILLUMINA);
    assertEquals(0L, pools.size());
  }

  @Test
  public void testListByIlluminaEmptySearchWithLimit() throws IOException {
    List<PoolImpl> pools = dao
        .listBySearchOffsetAndNumResultsAndPlatform(5, 3, "", "asc", "id", PlatformType.ILLUMINA);
    assertEquals(3L, pools.size());
  }

  @Test
  public void testListByIlluminaOffsetBadSortDir() throws IOException {
    List<PoolImpl> pools = dao.listByOffsetAndNumResults(5, 3, "BARK", "id", PlatformType.ILLUMINA);
    assertEquals(3, pools.size());
  }

  @Test
  public void testListByIlluminaSearchWithLimit() throws IOException {
    List<PoolImpl> pools = dao
        .listBySearchOffsetAndNumResultsAndPlatform(5, 3, "IPO", "asc", "id", PlatformType.ILLUMINA);
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

  @Ignore //fixme
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
    List<PoolImpl> pools = dao.listByOffsetAndNumResults(3, 3, "desc", "lastModified", PlatformType.ILLUMINA);
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
    Pool<?> pool = dao.get(1L);
    assertNotNull(pool);
    assertEquals(1L, pool.getId());
    assertNotNull(pool.getLastModified());
  }


}
