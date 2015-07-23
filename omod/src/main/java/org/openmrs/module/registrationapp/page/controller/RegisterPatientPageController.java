package org.openmrs.module.registrationapp.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.layout.web.address.AddressSupport;
import org.openmrs.layout.web.name.NameSupport;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.managehmo.ManageHMOAdminLister;
import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.openmrs.module.extendedpatientrecord.PatientExtended;
import org.openmrs.module.extendedpatientrecord.api.PatientExtendedService;
import org.openmrs.module.managehmo.ManageHMOAdminLister;
import org.openmrs.module.managehmo.HMO;
import org.openmrs.module.managedbrecord.api.ManageInsuranceSchemeService;
import org.openmrs.module.managedbrecord.InsuranceScheme;

import java.util.List;


public class RegisterPatientPageController {
    private static final Log log = LogFactory.getLog(RegisterPatientPageController.class);

    public void get(UiSessionContext sessionContext, PageModel model,
                    @RequestParam("appId") AppDescriptor app,
                    @ModelAttribute("patient") @BindParams Patient patient,
                    @ModelAttribute("patientextended") @BindParams PatientExtended patientExtended,
                    @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties) throws Exception {

        sessionContext.requireAuthentication();
        ManageInsuranceSchemeService insuranceSchemeService = Context.getService(ManageInsuranceSchemeService.class);
        ManageHMOAdminLister adminLister = new ManageHMOAdminLister();
        List<HMO> allHMOs  = adminLister.getAllHMOs();



        model.addAttribute("hmos", allHMOs);
        model.addAttribute("insuranceSchemes", insuranceSchemeService.getAllInsuranceSchemes());
        model.addAttribute("patientextended", patientExtended);



        addModelAttributes(model, patient, app, emrApiProperties.getPrimaryIdentifierType());
    }

    public void addModelAttributes(PageModel model, Patient patient, AppDescriptor app, PatientIdentifierType primaryIdentifierType) throws Exception {
        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);

        if (patient == null) {
        	patient = new Patient();
        }

        model.addAttribute("patient", patient);
        model.addAttribute("primaryIdentifierType", primaryIdentifierType);
        model.addAttribute("appId", app.getId());
        model.addAttribute("formStructure", formStructure);
        model.addAttribute("nameTemplate", NameSupport.getInstance().getDefaultLayoutTemplate());
        model.addAttribute("addressTemplate", AddressSupport.getInstance().getAddressTemplate().get(0));
        model.addAttribute("includeRegistrationDateSection", !app.getConfig().get("registrationEncounter").isNull()
                && !app.getConfig().get("allowRetrospectiveEntry").isNull()
                && app.getConfig().get("allowRetrospectiveEntry").getBooleanValue() );
        model.addAttribute("allowUnknownPatients", app.getConfig().get("allowUnknownPatients").getBooleanValue());
        model.addAttribute("allowManualIdentifier", app.getConfig().get("allowManualIdentifier").getBooleanValue());
        model.addAttribute("patientDashboardLink", app.getConfig().get("patientDashboardLink") !=null ?
                app.getConfig().get("patientDashboardLink").getTextValue() : null);
        model.addAttribute("enableOverrideOfAddressPortlet",
                Context.getAdministrationService().getGlobalProperty("addresshierarchy.enableOverrideOfAddressPortlet", "false"));

    } //addModelAttributes

}
