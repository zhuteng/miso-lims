package uk.ac.bbsrc.tgac.miso.webapp.controller;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import uk.ac.bbsrc.tgac.miso.core.data.KitComponent;
import uk.ac.bbsrc.tgac.miso.core.data.impl.kit.KitComponentImpl;
import uk.ac.bbsrc.tgac.miso.core.data.type.KitType;
import uk.ac.bbsrc.tgac.miso.core.data.type.PlatformType;
import uk.ac.bbsrc.tgac.miso.core.factory.DataObjectFactory;
import uk.ac.bbsrc.tgac.miso.core.manager.RequestManager;

/**
 * Created by zakm on 07/08/2015.
 */
@Controller
@RequestMapping("/kitcomponent")
@SessionAttributes("kitComponent")
public class EditKitComponentController {

    protected static final Logger log = LoggerFactory.getLogger(EditKitComponentController.class);

    @Autowired
    private RequestManager requestManager;

    @Autowired
    private DataObjectFactory dataObjectFactory;


    public void setDataObjectFactory(DataObjectFactory dataObjectFactory) {
        this.dataObjectFactory = dataObjectFactory;
    }

    public void setRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public ModelAndView setupForm(ModelMap model) throws IOException {

        return setupForm(KitComponentImpl.UNSAVED_ID, model);
    }

    @RequestMapping(value = "/{kitComponentId}", method = RequestMethod.GET)
    public ModelAndView setupForm(@PathVariable Long kitComponentId,
                                  ModelMap model) throws IOException {

            return new ModelAndView("/pages/editKitComponent.jsp", model);
    }


    @RequestMapping(method = RequestMethod.POST)
    public String processSubmit(@ModelAttribute("kitComponent") KitComponent kitComponent,
                                ModelMap model, SessionStatus session) throws IOException {
        try {
            requestManager.saveKitComponent(kitComponent);
            session.setComplete();
            model.clear();
            return "redirect:/miso/kitcomponent/" + kitComponent.getId();
        }
        catch (IOException ex) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to save KitComponent Descriptor", ex);
            }
            throw ex;
        }

    }

}
