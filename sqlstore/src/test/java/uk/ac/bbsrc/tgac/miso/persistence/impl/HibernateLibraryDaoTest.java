/**
 * 
 */
package uk.ac.bbsrc.tgac.miso.persistence.impl;

import static org.junit.Assert.*;

import java.io.IOException;

import org.h2.util.New;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.bbsrc.tgac.miso.AbstractDAOTest;
import uk.ac.bbsrc.tgac.miso.core.data.Library;
import uk.ac.bbsrc.tgac.miso.core.data.impl.LibraryImpl;

/**
 * @author Chris Salt
 *
 */
public class HibernateLibraryDaoTest extends AbstractDAOTest {
	@Autowired
	private SessionFactory sessionFactory;
	private HibernateLibraryDao libraryDao;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		libraryDao = new HibernateLibraryDao();
		libraryDao.setSessionFactory(sessionFactory);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#save(uk.ac.bbsrc.tgac.miso.core.data.Library)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public final void testSave() throws IOException {
		Library lib = new LibraryImpl();
		libraryDao.save(amazingLibrary);
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#get(long)}.
	 */
	@Test
	public final void testGet() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getByBarcode(java.lang.String)}.
	 */
	@Test
	public final void testGetByBarcode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getByBarcodeList(java.util.List)}.
	 */
	@Test
	public final void testGetByBarcodeList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getByAlias(java.lang.String)}.
	 */
	@Test
	public final void testGetByAlias() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getByPositionId(long)}.
	 */
	@Test
	public final void testGetByPositionId() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#lazyGet(long)}.
	 */
	@Test
	public final void testLazyGet() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getByIdentificationBarcode(java.lang.String)}.
	 */
	@Test
	public final void testGetByIdentificationBarcode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#listByLibraryDilutionId(long)}.
	 */
	@Test
	public final void testListByLibraryDilutionId() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#listBySampleId(long)}.
	 */
	@Test
	public final void testListBySampleId() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#listByProjectId(long)}.
	 */
	@Test
	public final void testListByProjectId() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getAdjacentLibrary(long, boolean)}.
	 */
	@Test
	public final void testGetAdjacentLibrary() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#listAll()}.
	 */
	@Test
	public final void testListAll() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#listAllWithLimit(long)}.
	 */
	@Test
	public final void testListAllWithLimit() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#count()}.
	 */
	@Test
	public final void testCount() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#listBySearch(java.lang.String)}.
	 */
	@Test
	public final void testListBySearch() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getByIdList(java.util.List)}.
	 */
	@Test
	public final void testGetByIdList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#remove(uk.ac.bbsrc.tgac.miso.core.data.Library)}.
	 */
	@Test
	public final void testRemove() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getLibraryTypeById(long)}.
	 */
	@Test
	public final void testGetLibraryTypeById() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getLibraryTypeByDescription(java.lang.String)}.
	 */
	@Test
	public final void testGetLibraryTypeByDescription() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getLibraryTypeByDescriptionAndPlatform(java.lang.String, uk.ac.bbsrc.tgac.miso.core.data.type.PlatformType)}.
	 */
	@Test
	public final void testGetLibraryTypeByDescriptionAndPlatform() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getLibrarySelectionTypeById(long)}.
	 */
	@Test
	public final void testGetLibrarySelectionTypeById() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getLibrarySelectionTypeByName(java.lang.String)}.
	 */
	@Test
	public final void testGetLibrarySelectionTypeByName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getLibraryStrategyTypeById(long)}.
	 */
	@Test
	public final void testGetLibraryStrategyTypeById() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getLibraryStrategyTypeByName(java.lang.String)}.
	 */
	@Test
	public final void testGetLibraryStrategyTypeByName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#listLibraryTypesByPlatform(java.lang.String)}.
	 */
	@Test
	public final void testListLibraryTypesByPlatform() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#listAllLibraryTypes()}.
	 */
	@Test
	public final void testListAllLibraryTypes() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#listAllLibrarySelectionTypes()}.
	 */
	@Test
	public final void testListAllLibrarySelectionTypes() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#listAllLibraryStrategyTypes()}.
	 */
	@Test
	public final void testListAllLibraryStrategyTypes() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#listBySearchOffsetAndNumResults(int, int, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testListBySearchOffsetAndNumResults() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#listByOffsetAndNumResults(int, int, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testListByOffsetAndNumResults() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#countLibrariesBySearch(java.lang.String)}.
	 */
	@Test
	public final void testCountLibrariesBySearch() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getChangeLogDAO()}.
	 */
	@Test
	public final void testGetChangeLogDAO() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#setChangeLogDAO(uk.ac.bbsrc.tgac.miso.core.store.ChangeLogStore)}.
	 */
	@Test
	public final void testSetChangeLogDAO() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getSecurityDAO()}.
	 */
	@Test
	public final void testGetSecurityDAO() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#setSecurityDAO(com.eaglegenomics.simlims.core.store.SecurityStore)}.
	 */
	@Test
	public final void testSetSecurityDAO() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link uk.ac.bbsrc.tgac.miso.persistence.impl.HibernateLibraryDao#getLibraryColumnSizes()}.
	 */
	@Test
	public final void testGetLibraryColumnSizes() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#Object()}.
	 */
	@Test
	public final void testObject() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#getClass()}.
	 */
	@Test
	public final void testGetClass() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#hashCode()}.
	 */
	@Test
	public final void testHashCode() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)}.
	 */
	@Test
	public final void testEquals() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#clone()}.
	 */
	@Test
	public final void testClone() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#toString()}.
	 */
	@Test
	public final void testToString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#notify()}.
	 */
	@Test
	public final void testNotify() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#notifyAll()}.
	 */
	@Test
	public final void testNotifyAll() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#wait(long)}.
	 */
	@Test
	public final void testWaitLong() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#wait(long, int)}.
	 */
	@Test
	public final void testWaitLongInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#wait()}.
	 */
	@Test
	public final void testWait() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#finalize()}.
	 */
	@Test
	public final void testFinalize() {
		fail("Not yet implemented"); // TODO
	}

}
