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

package uk.ac.bbsrc.tgac.miso.spring.ajax;

import com.eaglegenomics.simlims.core.Note;
import com.eaglegenomics.simlims.core.SecurityProfile;
import com.opensymphony.util.FileUtils;
import net.sf.ehcache.Cache;
import net.sf.json.JSONArray;
import org.apache.commons.codec.binary.Base64;
import org.krysalis.barcode4j.BarcodeDimension;
import org.krysalis.barcode4j.BarcodeGenerator;
import uk.ac.bbsrc.tgac.miso.core.data.*;
import com.eaglegenomics.simlims.core.User;
import com.eaglegenomics.simlims.core.manager.SecurityManager;
import net.sf.json.JSONObject;
import net.sourceforge.fluxion.ajax.Ajaxified;
import net.sourceforge.fluxion.ajax.util.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.bbsrc.tgac.miso.core.data.impl.ProjectOverview;
import uk.ac.bbsrc.tgac.miso.core.data.impl.SampleImpl;
import uk.ac.bbsrc.tgac.miso.core.data.type.ProgressType;
import uk.ac.bbsrc.tgac.miso.core.data.type.QcType;
import uk.ac.bbsrc.tgac.miso.core.exception.MisoNamingException;
import uk.ac.bbsrc.tgac.miso.core.exception.MisoPrintException;
import uk.ac.bbsrc.tgac.miso.core.manager.MisoFilesManager;
import uk.ac.bbsrc.tgac.miso.core.service.naming.MisoNamingScheme;
import uk.ac.bbsrc.tgac.miso.core.service.printing.MisoPrintService;
import uk.ac.bbsrc.tgac.miso.core.service.printing.context.PrintContext;
import uk.ac.bbsrc.tgac.miso.core.util.*;
import uk.ac.bbsrc.tgac.miso.core.factory.DataObjectFactory;
import uk.ac.bbsrc.tgac.miso.core.factory.barcode.BarcodeFactory;
import uk.ac.bbsrc.tgac.miso.core.factory.barcode.MisoJscriptFactory;
import uk.ac.bbsrc.tgac.miso.core.manager.PrintManager;
import uk.ac.bbsrc.tgac.miso.core.manager.RequestManager;
import uk.ac.bbsrc.tgac.miso.sqlstore.util.DbUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * uk.ac.bbsrc.tgac.miso.spring.ajax
 * <p/>
 * Info
 *
 * @author Michal Zak
 * @since 0.0.2
 */
@Ajaxified
public class KitComponentControllerHelperService {
    protected static final Logger log = LoggerFactory.getLogger(KitComponentControllerHelperService.class);
    @Autowired
    private SecurityManager securityManager;
    @Autowired
    private RequestManager requestManager;
    @Autowired
    private MisoFilesManager misoFileManager;
    @Autowired
    private DataObjectFactory dataObjectFactory;



    public JSONObject getKitInfoByReferenceNumber(HttpSession session, JSONObject json) {
        try {
            JSONObject response = new JSONObject();


            KitComponentDescriptor kitComponentDescriptor =  requestManager.getKitComponentDescriptorByReferenceNumber(json.getString("referenceNumber"));

            if(kitComponentDescriptor !=null) {
                KitDescriptor kitDescriptor = kitComponentDescriptor.getKitDescriptor();

                //kit descriptor info
                response.put("name", kitDescriptor.getName());
                response.put("version", kitDescriptor.getVersion());
                response.put("manufacturer", kitDescriptor.getManufacturer());
                response.put("partNumber", kitDescriptor.getPartNumber());
                response.put("kitType", kitDescriptor.getKitType().getKey());
                response.put("platformType", kitDescriptor.getPlatformType().getKey());
                response.put("units", kitDescriptor.getUnits());
                response.put("kitValue", kitDescriptor.getKitValue());

                //kit component descriptor info
                response.put("componentName", kitComponentDescriptor.getName());
            }
            return response;
        }
        catch (IOException e) {
            log.debug("Failed", e);
            return JSONUtils.SimpleJSONError("Failed: " + e.getMessage());
        }
    }


    public void setSecurityManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    public void setDataObjectFactory(DataObjectFactory dataObjectFactory) {
        this.dataObjectFactory = dataObjectFactory;
    }

    public void setMisoFileManager(MisoFilesManager misoFileManager) {
        this.misoFileManager = misoFileManager;
    }





    //TODO: MIGHT BE USEFUL FOR SAVING THE WHOLE LOGGING SESSION
    /*
    public JSONObject bulkSaveKitComponents(HttpSession session, JSONObject json) {
        if (json.has("kitComponents")) {
            try {
                Project p = requestManager.getProjectById(json.getLong("projectId"));
                SecurityProfile sp = p.getSecurityProfile();
                JSONArray a = JSONArray.fromObject(json.get("samples"));
                Set<Sample> saveSet = new HashSet<Sample>();

                for (JSONObject j : (Iterable<JSONObject>) a) {
                    try {
                        String alias = j.getString("alias");

                        if (sampleNamingScheme.validateField("alias", alias)) {
                            String descr = j.getString("description");
                            String scientificName = j.getString("scientificName");
                            DateFormat df = new SimpleDateFormat("dd/mm/yyyy");
                            String type = j.getString("sampleType");
                            String locationBarcode = j.getString("locationBarcode");

                            Sample news = new SampleImpl();
                            news.setProject(p);
                            news.setAlias(alias);
                            news.setDescription(descr);
                            news.setScientificName(scientificName);
                            news.setSecurityProfile(sp);
                            news.setSampleType(type);
                            news.setLocationBarcode(locationBarcode);

                            if (j.has("receivedDate") && !"".equals(j.getString("receivedDate"))) {
                                Date date = df.parse(j.getString("receivedDate"));
                                news.setReceivedDate(date);
                            }

                            if (!j.getString("note").equals("")) {
                                Note note = new Note();
                                note.setOwner(sp.getOwner());
                                note.setText(j.getString("note"));
                                note.setInternalOnly(true);

                                if (j.has("receivedDate") && !"".equals(j.getString("receivedDate"))) {
                                    Date date = df.parse(j.getString("receivedDate"));
                                    note.setCreationDate(date);
                                }
                                else {
                                    note.setCreationDate(new Date());
                                }

                                news.setNotes(Arrays.asList(note));
                            }

                            saveSet.add(news);
                        }
                        else {
                            return JSONUtils.SimpleJSONError("The following sample alias doesn't conform to the chosen naming scheme (" + sampleNamingScheme.getValidationRegex("alias") + ") or already exists: " + j.getString("alias"));
                        }
                    }
                    catch (ParseException e) {
                        e.printStackTrace();
                        return JSONUtils.SimpleJSONError("Cannot parse date for sample " + j.getString("alias"));
                    }
                    catch (MisoNamingException e) {
                        e.printStackTrace();
                        return JSONUtils.SimpleJSONError("Cannot validate sample alias " + j.getString("alias") + ": " + e.getMessage());
                    }
                }

                Set<Sample> samples = new HashSet<Sample>(requestManager.listAllSamples());
                // relative complement to find objects that aren't already persisted
                Set<Sample> complement = LimsUtils.relativeComplementByProperty(
                        Sample.class,
                        "getAlias",
                        saveSet,
                        samples);

                if (complement != null && !complement.isEmpty()) {
                    List<Sample> sortedList = new ArrayList<Sample>(complement);
                    List<String> savedSamples = new ArrayList<String>();
                    List<String> taxonErrorSamples = new ArrayList<String>();
                    Collections.sort(sortedList, new AliasComparator(Sample.class));

                    for (Sample sample : sortedList) {
                        if ((Boolean) session.getServletContext().getAttribute("taxonLookupEnabled")) {
                            log.info("Checking taxon: " + sample.getScientificName());
                            String taxon = TaxonomyUtils.checkScientificNameAtNCBI(sample.getScientificName());
                            if (taxon != null) {
                                sample.setTaxonIdentifier(taxon);
                                taxonErrorSamples.add(sample.getAlias());
                            }
                        }

                        try {
                            requestManager.saveSample(sample);
                            savedSamples.add(sample.getAlias());
                            log.info("Saved: " + sample.getAlias());
                        }
                        catch (IOException e) {
                            log.error("Couldn't save: " + sample.getAlias());
                            e.printStackTrace();
                        }
                    }

                    Map<String, Object> response = new HashMap<String, Object>();
                    response.put("savedSamples", JSONArray.fromObject(savedSamples));
                    response.put("taxonErrorSamples", JSONArray.fromObject(taxonErrorSamples));

                    return JSONUtils.JSONObjectResponse(response);
                }
                else {
                    return JSONUtils.SimpleJSONError("Error in saving samples - perhaps samples specified already exist in the database with a given alias?");
                }
            }
            catch (NoSuchMethodException e) {
                e.printStackTrace();
                return JSONUtils.SimpleJSONError("Cannot save samples for project " + json.getLong("projectId") + ": " + e.getMessage());
            }
            catch (IOException e) {
                e.printStackTrace();
                return JSONUtils.SimpleJSONError("Cannot save samples for project " + json.getLong("projectId") + ": " + e.getMessage());
            }
        }
        else {
            return JSONUtils.SimpleJSONError("No samples specified");
        }
    }
    */


}
