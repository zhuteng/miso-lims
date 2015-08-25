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
import net.sf.json.JSON;
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
import uk.ac.bbsrc.tgac.miso.core.data.impl.kit.KitComponentImpl;
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
import java.sql.Timestamp;
import java.text.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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


    public JSONObject listAllKitComponentsForTable(HttpSession session, JSONObject json){

        JSONArray componentsArr = new JSONArray();
        try {
            Collection<KitComponent> kitComponents = requestManager.listAllKitComponents();
            KitComponentDescriptor kitComponentDescriptor;
            KitDescriptor kitDescriptor;

            JSONObject component = new JSONObject();
            NumberFormat formatter = new DecimalFormat("£#0.00");

            for (KitComponent kitComponent : kitComponents){
                kitComponentDescriptor = kitComponent.getKitComponentDescriptor();
                kitDescriptor = kitComponentDescriptor.getKitDescriptor();


                component.put("ID", kitComponent.getId());
                component.put("Kit Name", kitDescriptor.getName());
                component.put("Component Name", kitComponentDescriptor.getName());
                component.put("Version", kitDescriptor.getVersion());
                component.put("Manufacturer", kitDescriptor.getManufacturer());
                component.put("Part Number", kitDescriptor.getPartNumber());
                component.put("Type", kitDescriptor.getKitType());
                component.put("Platform", kitDescriptor.getPlatformType());
                component.put("Units", kitDescriptor.getUnits());
                component.put("Value", formatter.format(kitDescriptor.getKitValue()));
                component.put("Reference Number", kitComponentDescriptor.getReferenceNumber());
                component.put("Identification Barcode", kitComponent.getIdentificationBarcode());
                component.put("Lot Number", kitComponent.getLotNumber());
                component.put("Location Barcode", kitComponent.getLocationBarcode());
                component.put("Received Date", kitComponent.getKitReceivedDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                component.put("Expiry Date", kitComponent.getKitExpiryDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                component.put("Exhausted", kitComponent.isExhausted());
                component.put("Expiry State", DateUtils.getExpiryState(kitComponent.getKitExpiryDate()).ordinal());
                //0 - expired
                //1 - soon to expire
                //2 - good to use


                componentsArr.add(component);




            }
            JSONObject jsonComponents = new JSONObject();
            jsonComponents.put("components", componentsArr);    //nesting might not be necessary
            return jsonComponents;
        }

        catch (IOException ex) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to list kit components", ex);
            }
            return JSONUtils.SimpleJSONError("error");


        }



    }

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

    public JSONObject getKitInfoByIdentificationBarcode(HttpSession session, JSONObject json) {
        try {
            JSONObject response = new JSONObject();


            KitComponent kitComponent =  requestManager.getKitComponentByIdentificationBarcode(json.getString("identificationBarcode"));

            if(kitComponent !=null) {
                KitComponentDescriptor kitComponentDescriptor = kitComponent.getKitComponentDescriptor();
                KitDescriptor kitDescriptor = kitComponentDescriptor.getKitDescriptor();

                //kit descriptor info
                response.put("name", kitDescriptor.getName());
                //kit component descriptor info
                response.put("componentName", kitComponentDescriptor.getName());
                response.put("referenceNumber", kitComponentDescriptor.getReferenceNumber());
                response.put("lotNumber", kitComponent.getLotNumber());
                response.put("receivedDate", kitComponent.getKitReceivedDate().toString());
                response.put("expiryDate", kitComponent.getKitExpiryDate().toString());
                response.put("locationBarcode", kitComponent.getLocationBarcode());
                response.put("exhausted", kitComponent.isExhausted());
            }
            return response;
        }
        catch (IOException e) {
            log.debug("Failed", e);
            return JSONUtils.SimpleJSONError("Failed: " + e.getMessage());
        }
    }

    public JSONObject exhaustKitComponent(HttpSession session, JSONObject json){

            String identificationBarcode = json.getString("identificationBarcode");
            String locationBarcode = json.getString("locationBarcodeNew");

            try{
            KitComponent kitComponent = requestManager.getKitComponentByIdentificationBarcode(identificationBarcode);


                kitComponent.setExhausted(true);
                kitComponent.setLocationBarcode(locationBarcode);
                requestManager.saveKitComponent(kitComponent);

                //log the change
                JSONObject logObject = new JSONObject();
                logObject.put("kitComponentId", kitComponent.getId());
                logObject.put("locationBarcodeNew", json.getString("locationBarcodeNew"));
                logObject.put("locationBarcodeOld", json.getString("locationBarcodeOld"));
                logObject.put("exhausted", true);

                //LOG
                logLocationChange(logObject);

            } catch (IOException e) {
                e.printStackTrace();
                return JSONUtils.SimpleJSONError("Something went wrong");
            }

        return JSONUtils.SimpleJSONResponse("ok");

    }

    public JSONObject changeLocation(HttpSession session, JSONObject json){
        String identificationBarcode = json.getString("identificationBarcode");
        String locationBarcode = json.getString("locationBarcodeNew");


        try{
            KitComponent kitComponent = requestManager.getKitComponentByIdentificationBarcode(identificationBarcode);

            kitComponent.setLocationBarcode(locationBarcode);
            requestManager.saveKitComponent(kitComponent);

            //log the change
            JSONObject logObject = new JSONObject();
            logObject.put("kitComponentId", kitComponent.getId());
            logObject.put("locationBarcodeNew", json.getString("locationBarcodeNew"));
            logObject.put("locationBarcodeOld", json.getString("locationBarcodeOld"));
            logObject.put("exhausted", false);
            //LOG
            logLocationChange(logObject);

        } catch (IOException e) {
            e.printStackTrace();
            return JSONUtils.SimpleJSONError("Something went wrong");
        }

        return JSONUtils.SimpleJSONResponse("ok");

    }

    public void logLocationChange (JSONObject json) throws IOException {
        User user = securityManager.getUserByLoginName(SecurityContextHolder.getContext().getAuthentication().getName());
        long userId = user.getUserId();

        LocalDateTime now = LocalDateTime.now();

        Timestamp nowTimestamp = Timestamp.valueOf(now);

        json.put("userId", userId);
        json.put("logDate", nowTimestamp);

        try{
            requestManager.saveKitChangeLog(json);
        }catch (IOException e){
            e.printStackTrace();
        }





    }



    public JSONObject saveKitComponents(HttpSession session, JSONObject json){
        JSONArray a = JSONArray.fromObject(json.get("components"));

        for (JSONObject j : (Iterable<JSONObject>) a) {
            KitComponent kitComponent = new KitComponentImpl();

            String identificationBarcode = j.getString("identificationBarcode");
            String locationBarcode = j.getString("locationBarcode");
            String lotNumber = j.getString("lotNumber");
            LocalDate receivedDate = DateUtils.asLocalDate(j.getString("receivedDate"));
            LocalDate expiryDate = DateUtils.asLocalDate(j.getString("expiryDate"));
            String referenceNumber = j.getString("referenceNumber");
            boolean exhausted = false;


            kitComponent.setIdentificationBarcode(identificationBarcode);
            kitComponent.setLocationBarcode(locationBarcode);
            kitComponent.setLotNumber(lotNumber);
            kitComponent.setKitReceivedDate(receivedDate);
            kitComponent.setKitExpiryDate(expiryDate);
            kitComponent.setExhausted(exhausted);

            try {
                kitComponent.setKitComponentDescriptor(requestManager.getKitComponentDescriptorByReferenceNumber(referenceNumber));
                requestManager.saveKitComponent(kitComponent);

                //log the change
                JSONObject logObject = new JSONObject();
                logObject.put("kitComponentId", kitComponent.getId());
                logObject.put("locationBarcodeNew", locationBarcode);
                logObject.put("locationBarcodeOld", "N/A (KIT WAS NOT LOGGED BEFORE)");
                logObject.put("exhausted", false);
                //LOG
                logLocationChange(logObject);

            } catch (IOException e) {
                e.printStackTrace();
                return JSONUtils.SimpleJSONError("Something went wrong");
            }
        }
        return JSONUtils.SimpleJSONResponse("ok");

    }

    public JSONObject getKitChangeLog(HttpSession session, JSONObject json){
        JSONObject result = new JSONObject();
        try {
            JSONArray changeLog = requestManager.getKitChangeLog();


            result.put("changeLog", changeLog);

        }catch (IOException e) {
            e.printStackTrace();
            return JSONUtils.SimpleJSONError("Something went wrong");
        }

        return result;


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

}
