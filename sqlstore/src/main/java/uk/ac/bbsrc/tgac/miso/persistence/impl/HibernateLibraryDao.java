/*
 * Copyright (c) 2012. The Genome Analysis Centre, Norwich, UK
 * MISO project contacts: Robert Davey, Mario Caccamo @ TGAC
 * *********************************************************************
 *
 * This file is part of MISO.
 *
 * MISO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MISO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MISO.  If not, see <http://www.gnu.org/licenses/>.
 *
 * *********************************************************************
 */

package uk.ac.bbsrc.tgac.miso.persistence.impl;

import static uk.ac.bbsrc.tgac.miso.core.util.LimsUtils.isStringEmptyOrNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.eaglegenomics.simlims.core.SecurityProfile;
import com.eaglegenomics.simlims.core.store.SecurityStore;

import uk.ac.bbsrc.tgac.miso.core.data.Boxable;
import uk.ac.bbsrc.tgac.miso.core.data.Library;
import uk.ac.bbsrc.tgac.miso.core.data.LibraryQC;
import uk.ac.bbsrc.tgac.miso.core.data.type.LibrarySelectionType;
import uk.ac.bbsrc.tgac.miso.core.data.type.LibraryStrategyType;
import uk.ac.bbsrc.tgac.miso.core.data.type.LibraryType;
import uk.ac.bbsrc.tgac.miso.core.data.type.PlatformType;
import uk.ac.bbsrc.tgac.miso.core.factory.DataObjectFactory;
import uk.ac.bbsrc.tgac.miso.core.service.naming.MisoNamingScheme;
import uk.ac.bbsrc.tgac.miso.core.store.BoxStore;
import uk.ac.bbsrc.tgac.miso.core.store.ChangeLogStore;
import uk.ac.bbsrc.tgac.miso.core.store.IndexStore;
import uk.ac.bbsrc.tgac.miso.core.store.LibraryStore;
import uk.ac.bbsrc.tgac.miso.core.store.Store;
import uk.ac.bbsrc.tgac.miso.persistence.LibraryAdditionalInfoDao;

/**
 * uk.ac.bbsrc.tgac.miso.persistence.impl
 * <p/>
 * Info
 *
 * @author Chris Salt
 */
@Transactional(rollbackFor = Exception.class)
public class HibernateLibraryDao implements LibraryStore {

	protected static final Logger log = LoggerFactory.getLogger(HibernateLibraryDao.class);
	private Store<SecurityProfile> securityProfileDAO;
	private boolean autoGenerateIdentificationBarcodes;
	private ChangeLogStore changeLogDAO;
	private SecurityStore securityDAO;
	private BoxStore boxDAO;

	@Autowired
	private MisoNamingScheme<Library> libraryNamingScheme;

	public MisoNamingScheme<Library> getLibraryNamingScheme() {
		return libraryNamingScheme;
	}

	public void setLibraryNamingScheme(MisoNamingScheme<Library> libraryNamingScheme) {
		this.libraryNamingScheme = libraryNamingScheme;
	}

	@Autowired
	private MisoNamingScheme<Library> namingScheme;

	@Override
	public MisoNamingScheme<Library> getNamingScheme() {
		return namingScheme;
	}

	@Override
	public void setNamingScheme(MisoNamingScheme<Library> namingScheme) {
		this.namingScheme = namingScheme;
	}

	public void setDataObjectFactory(DataObjectFactory dataObjectFactory) {
	}

	public Store<SecurityProfile> getSecurityProfileDAO() {
		return securityProfileDAO;
	}

	public void setSecurityProfileDAO(Store<SecurityProfile> securityProfileDAO) {
		this.securityProfileDAO = securityProfileDAO;
	}

	public BoxStore getBoxDAO() {
		return boxDAO;
	}

	public void setBoxDAO(BoxStore boxDAO) {
		this.boxDAO = boxDAO;
	}

	public void setAutoGenerateIdentificationBarcodes(boolean autoGenerateIdentificationBarcodes) {
		this.autoGenerateIdentificationBarcodes = autoGenerateIdentificationBarcodes;
	}

	public boolean getAutoGenerateIdentificationBarcodes() {
		return autoGenerateIdentificationBarcodes;
	}

	/**
	 * Generates a unique barcode. Note that the barcode will change when the
	 * alias is changed.
	 * 
	 * @param library
	 */
	public void autoGenerateIdBarcode(Library library) {
		String barcode = library.getName() + "::" + library.getAlias();
		library.setIdentificationBarcode(barcode);
	}

	@Autowired
	private SessionFactory sessionFactory;

	private Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	private Criteria getCriteria() {
		return getCurrentSession().createCriteria(Library.class);
	}

	@Override
	public long save(Library library) throws IOException {
		if (library.isEmpty()) {
			boxDAO.removeBoxableFromBox(library);
			library.setVolume(0D);
		}

		getCurrentSession().saveOrUpdate(library);
		return library.getId();
	}

	@Override
	public Library get(long libraryId) throws IOException {
		return (Library) getCriteria().add(Restrictions.eq("libraryId", libraryId)).uniqueResult();
	}

	@Override
	public Library getByBarcode(String barcode) throws IOException {
		return (Library) getCriteria().add(Restrictions.eq("barcode", barcode)).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Library> getByBarcodeList(List<String> barcodeList) {
		return getCriteria().add(Restrictions.in("identificationBarcode", barcodeList)).list();
	}

	@Override
	public Library getByAlias(String alias) throws IOException {
		return (Library) getCriteria().add(Restrictions.eq("alias", alias));
	}

	@Override
	public Boxable getByPositionId(long positionId) {
		return (Boxable) getCriteria().add(Restrictions.eqOrIsNull("boxPositionId", positionId)).uniqueResult();
	}

	@Deprecated
	@Override
	public Library lazyGet(long libraryId) throws IOException {
		return this.get(libraryId);
	}

	public Library getByIdentificationBarcode(String barcode) throws IOException {

		return (Library) getCriteria().add(Restrictions.eq("identificationBarcode", barcode)).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Library> listByLibraryDilutionId(long dilutionId) throws IOException {
		return getCriteria().createAlias("LibraryDilution", "ld")
				.add(Restrictions.eq("ld.library_libraryId", dilutionId)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Library> listBySampleId(long sampleId) throws IOException {
		return getCriteria().add(Restrictions.eq("sample_sampleId", sampleId)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Library> listByProjectId(long projectId) throws IOException {
		return getCriteria().createAlias("Sample", "sam").add(Restrictions.eq("sam.project_projectId", projectId))
				.list();
	}

	@Override
	public Library getAdjacentLibrary(long libraryId, boolean before) throws IOException {
		final Library current = (Library) getCriteria().add(Restrictions.eq("libraryId", libraryId)).uniqueResult();
		final long sampleId = current.getSample().getId();
		final Criteria criteria = getCriteria().add(Restrictions.eq("sample_sampleId", sampleId));
		if (before) {
			criteria.add(Restrictions.lt("libraryId", libraryId)).setProjection(Projections.max("libraryId"));
		} else {
			criteria.add(Restrictions.gt("libraryId", libraryId)).setProjection(Projections.min("libraryId"));
		}
		return (Library) criteria.list().get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Library> listAll() throws IOException {
		return getCriteria().list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Library> listAllWithLimit(long limit) throws IOException {
		return getCriteria().setMaxResults((int) limit).list();
	}

	@Override
	public int count() throws IOException {
		return (int) getCriteria().setProjection(Projections.rowCount()).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Library> listBySearch(String query) {
		return getCriteria().add(Restrictions.disjunction().add(Restrictions.ilike("name", query))
				.add(Restrictions.ilike("identificationBarcode", query)).add(Restrictions.ilike("alias", query))
				.add(Restrictions.ilike("description", query))).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Library> getByIdList(List<Long> idList) throws IOException {
		return getCriteria().add(Restrictions.in("libraryId", idList)).list();
	}

	@Override
	public boolean remove(Library library) throws IOException {
		boolean success = false;
		// TODO : This is dodgey! what happens if it goes wrong?
		if (library.isDeletable()) {
			for (LibraryQC lqc : library.getLibraryQCs()) {
				getCurrentSession().delete(lqc);
			}
			getCurrentSession().delete(library);

			success = true;
		}
		return success;
	}

	@Override
	public LibraryType getLibraryTypeById(long libraryTypeId) throws IOException {
		final Criteria criteria = getCurrentSession().createCriteria(LibraryType.class);
		return (LibraryType) criteria.add(Restrictions.eq("libraryTypeId", libraryTypeId)).list().get(0);
	}

	@Override
	public LibraryType getLibraryTypeByDescription(String description) throws IOException {
		final Criteria criteria = getCurrentSession().createCriteria(LibraryType.class);
		return (LibraryType) criteria.add(Restrictions.eq("description", description)).list().get(0);
	}

	@Override
	public LibraryType getLibraryTypeByDescriptionAndPlatform(String description, PlatformType platformType)
			throws IOException {
		final Criteria criteria = getCurrentSession().createCriteria(LibraryType.class);
		return (LibraryType) criteria.add(Restrictions.eq("description", description))
				.add(Restrictions.eq("platformType", platformType)).list().get(0);
	}

	@Override
	public LibrarySelectionType getLibrarySelectionTypeById(long librarySelectionTypeId) throws IOException {
		final Criteria criteria = getCurrentSession().createCriteria(LibrarySelectionType.class);
		return (LibrarySelectionType) criteria.add(Restrictions.eq("librarySelectionTypeId", librarySelectionTypeId))
				.list().get(0);
	}

	@Override
	public LibrarySelectionType getLibrarySelectionTypeByName(String name) throws IOException {
		final Criteria criteria = getCurrentSession().createCriteria(LibrarySelectionType.class);

		// TODO : there are a few of these .get(0) methods here.
		// They should probably throw using uniqueResult.
		return (LibrarySelectionType) criteria.add(Restrictions.eq("name", name)).list().get(0);
	}

	@Override
	public LibraryStrategyType getLibraryStrategyTypeById(long libraryStrategyTypeId) throws IOException {
		final Criteria criteria = getCurrentSession().createCriteria(LibraryStrategyType.class);
		return (LibraryStrategyType) criteria.add(Restrictions.eq("libraryStrategyTypeId", libraryStrategyTypeId))
				.list().get(0);
	}

	@Override
	public LibraryStrategyType getLibraryStrategyTypeByName(String name) throws IOException {
		final Criteria criteria = getCurrentSession().createCriteria(LibraryStrategyType.class);
		return (LibraryStrategyType) criteria.add(Restrictions.eq("name", name)).list().get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LibraryType> listLibraryTypesByPlatform(String platformType) throws IOException {
		final Criteria criteria = getCurrentSession().createCriteria(LibraryType.class);
		return criteria.add(Restrictions.eq("platformType", platformType)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LibraryType> listAllLibraryTypes() throws IOException {
		final Criteria criteria = getCurrentSession().createCriteria(LibraryType.class);
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LibrarySelectionType> listAllLibrarySelectionTypes() throws IOException {
		final Criteria criteria = getCurrentSession().createCriteria(LibrarySelectionType.class);
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	// TODO: These could probably be made generic? List<T> listClass(T class)?
	public List<LibraryStrategyType> listAllLibraryStrategyTypes() throws IOException {
		final Criteria criteria = getCurrentSession().createCriteria(LibraryStrategyType.class);
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	// TODO : Consider using hibernate search.
	public List<Library> listBySearchOffsetAndNumResults(int offset, int limit, String querystr, String sortDir,
			String sortCol) throws IOException {
		if (offset < 0 || limit < 0)
			throw new IOException("Limit and Offset must be greater than zero");
		if (isStringEmptyOrNull(querystr)) {
			return listByOffsetAndNumResults(offset, limit, sortDir, sortCol);
		} else {
			final Criteria criteria = getCriteria();
			if ("asc".equalsIgnoreCase(sortDir)) {
				criteria.addOrder(Order.asc(sortCol));
			} else if ("desc".equalsIgnoreCase(sortDir)) {
				criteria.addOrder(Order.desc(sortCol));
			}
			criteria.setFirstResult(offset);
			criteria.setMaxResults(limit);
			// add search to all relevant fields.
			criteria.add(Restrictions.disjunction().add(Restrictions.ilike("name", querystr))
					.add(Restrictions.ilike("identificationBarcode", querystr))
					.add(Restrictions.ilike("alias", querystr)).add(Restrictions.ilike("description", querystr)));
			return criteria.list();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Library> listByOffsetAndNumResults(int offset, int limit, String sortDir, String sortCol)
			throws IOException {
		if (offset < 0 || limit < 0) {
			throw new IOException("Limit and Offset must be greater than zero");
		}
		final Criteria criteria = getCriteria();
		if ("asc".equalsIgnoreCase(sortDir)) {
			criteria.addOrder(Order.asc(sortCol));
		} else if ("desc".equalsIgnoreCase(sortDir)) {
			criteria.addOrder(Order.desc(sortCol));
		}
		criteria.setFirstResult(offset);
		criteria.setMaxResults(limit);
		return criteria.list();
	}

	@Override
	public long countLibrariesBySearch(String querystr) throws IOException {

		if (isStringEmptyOrNull(querystr)) {
			return (count());
		} else {
			final Criteria criteria = getCriteria();
			criteria.add(Restrictions.disjunction().add(Restrictions.ilike("name", querystr))
					.add(Restrictions.ilike("identificationBarcode", querystr))
					.add(Restrictions.ilike("alias", querystr)).add(Restrictions.ilike("description", querystr)));

			return (int) criteria.setProjection(Projections.rowCount()).uniqueResult();
		}
	}

	public ChangeLogStore getChangeLogDAO() {
		return changeLogDAO;
	}

	public void setChangeLogDAO(ChangeLogStore changeLogDAO) {
		this.changeLogDAO = changeLogDAO;
	}
	
	public SecurityStore getSecurityDAO() {
		return securityDAO;
	}

	public void setSecurityDAO(SecurityStore securityDAO) {
		this.securityDAO = securityDAO;
	}

	@Override
	public Map<String, Integer> getLibraryColumnSizes() throws IOException {
		throw new NotImplementedException("Why do you even want this?!");
		// return DbUtils.getColumnSizes(template, TABLE_NAME);
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		
	}
}
