package org.openmrs.module.registrationapp.page.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.layout.web.address.AddressSupport;
import org.openmrs.layout.web.name.NameSupport;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.registrationapp.RegistrationAppUiUtils;
import org.openmrs.module.registrationapp.form.RegisterPatientFormBuilder;
import org.openmrs.module.registrationapp.model.NavigableFormStructure;
import org.openmrs.module.registrationapp.model.Section;
import org.openmrs.module.uicommons.UiCommonsConstants;
import org.openmrs.module.uicommons.util.InfoErrorMessageUtil;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.session.Session;
import org.openmrs.validator.PatientValidator;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.openmrs.module.extendedpatientrecord.PatientExtended;
import org.openmrs.module.extendedpatientrecord.AdministerList;
import org.openmrs.module.managehmo.HMO;
import org.openmrs.module.managehmo.ManageHMOAdminLister;


import java.util.Calendar;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public class EditSectionPageController {

    protected final Log log = LogFactory.getLog(EditSectionPageController.class);
    private AdministerList administerList = new AdministerList();

    public void get(UiSessionContext sessionContext, PageModel model,
                    @RequestParam("patientId") Patient patient,
                    @RequestParam("appId") AppDescriptor app,
                    @RequestParam(value = "returnUrl", required = false) String returnUrl,
                    @RequestParam("sectionId") String sectionId,
                    @SpringBean("adminService") AdministrationService administrationService) throws Exception {

        sessionContext.requireAuthentication();

        /* todo - remove log.error test */
        log.error("Getting required patient Id: = "+patient.getId());
        PatientExtended patientExtended = administerList.getPatientExtended(patient.getId()); //






        if (patientExtended == null) {
            patientExtended = new PatientExtended(); // If no extended record exist for this patient...
            log.error("No record for this patient extended!");
        }else {
            patientExtended.setId(patient.getId());
            log.error("Record exist for patient extended.");
        }



        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);
        addModelAttributes(model, patient, patientExtended, formStructure.getSections().get(sectionId), administrationService, returnUrl,
                app);
    }

    /**
     * @should void the old person address and replace it with a new one when it is edited
     * @should void the old person address and replace it with a new one when it is edited
     * @should not void the existing address if there are no changes
     */
    public String post(UiSessionContext sessionContext, PageModel model,
                       @RequestParam("patientId") @BindParams Patient patient,
                       @ModelAttribute("patientextended") @BindParams PatientExtended patientExtended,
                       @BindParams PersonAddress address,
                       @BindParams PersonName name,
                       @RequestParam(value="birthdateYears", required = false) Integer birthdateYears,
                       @RequestParam(value="birthdateMonths", required = false) Integer birthdateMonths,
                       @RequestParam("appId") AppDescriptor app,
                       @RequestParam("sectionId") String sectionId,
                       @RequestParam("returnUrl") String returnUrl,
                       @SpringBean("patientService") PatientService patientService,
                       @SpringBean("adminService") AdministrationService administrationService, HttpServletRequest request,
                       @SpringBean("messageSourceService") MessageSourceService messageSourceService, Session session,
                       @SpringBean("patientValidator") PatientValidator patientValidator, UiUtils ui) throws Exception {

        sessionContext.requireAuthentication();


        /* todo delete/remove log.error */
        log.error("Saving/updating new patient extended record: ");


        // handle person name, if present
        if (patient.getPersonName() != null && name != null && StringUtils.isNotBlank(name.getFullName())) {  // bit of a hack because it seems that in this case name is never null, so we
            PersonName currentName = patient.getPersonName();
            if (!currentName.equalsContent(name)) {
                //void the old name and replace it with the new one
                patient.addName(name);
                currentName.setVoided(true);
            }
        }

        // handle birthdate, if present
        if (patient.getBirthdate() == null && birthdateYears != null) {
            patient.setBirthdateEstimated(true);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -birthdateYears);
            if (birthdateMonths != null) {
                calendar.add(Calendar.MONTH, -birthdateMonths);
            }
            patient.setBirthdate(calendar.getTime());
        }


        // handle person address, if present
        if (address != null && !address.isBlank()) {
            PersonAddress currentAddress = patient.getPersonAddress();
            if (currentAddress != null) {
                if (!currentAddress.equalsContent(address)) {
                    //void the old address and replace it with the new one
                    patient.addAddress(address);
                    currentAddress.setVoided(true);
                }
            }
            else {
                patient.addAddress(address);
            }
        }

        NavigableFormStructure formStructure = RegisterPatientFormBuilder.buildFormStructure(app);

        BindingResult errors = new BeanPropertyBindingResult(patient, "patient");
        patientValidator.validate(patient, errors);
        RegistrationAppUiUtils.validateLatitudeAndLongitudeIfNecessary(address, errors);

        if (formStructure != null) {
            RegisterPatientFormBuilder.resolvePersonAttributeFields(formStructure, patient, request.getParameterMap());
        }

        if (!errors.hasErrors()) {
            try {

                //The person address changes get saved along as with the call to save patient
                patientService.savePatient(patient);

                if (patientExtended.getHmo() == null || patientExtended.getHmo() <=0){
                    patientExtended.setPatientType("Facility Patient");
                }else{
                    patientExtended.setPatientType("HMO Patient"); }


                log.error("Gotten HMO: "+patientExtended.getHmo());
                log.error("Gotten Next of Kin Firstname: "+ patientExtended.getNextOfKinFirstname());
                log.error("Gotten Next of Kin Address: "+patientExtended.getNextOfKinAddress());

                /* ---------- get existing patient record, modify fields, and save ----------- */
                PatientExtended reqPatientExtended = administerList.getPatientExtended(patient.getId());

                if (reqPatientExtended != null){
                    reqPatientExtended.setPatientType(patientExtended.getPatientType());
                    if (patientExtended.getHmo() != null)reqPatientExtended.setHmo(patientExtended.getHmo());
                    if (patientExtended.getNextOfKinFirstname() != null) reqPatientExtended.setNextOfKinFirstname(patientExtended.getNextOfKinFirstname());
                    if (patientExtended.getNextOfKinAddress() != null) reqPatientExtended.setNextOfKinAddress(patientExtended.getNextOfKinAddress());
                    if (patientExtended.getNextOfKinEmail() != null) reqPatientExtended.setNextOfKinEmail(patientExtended.getNextOfKinEmail());
                    if (patientExtended.getNextOfKinLastname() !=null)reqPatientExtended.setNextOfKinLastname(patientExtended.getNextOfKinLastname());
                    if (patientExtended.getNextOfKinPhoneNo() !=null) reqPatientExtended.setNextOfKinPhoneNo(patientExtended.getNextOfKinPhoneNo());
                    if (patientExtended.getNextOfKinRelationship() !=null)reqPatientExtended.setNextOfKinRelationship(patientExtended.getNextOfKinRelationship());

                    administerList.updatePatientExtended(reqPatientExtended);// update record
                }else{
                    patientExtended.setId(patient.getId());
                    administerList.savePatientExtended(patientExtended); // save record
                }


                //patientExtended.setId(patient.getId()); // set patient id for patient extended record...
                /*if (administerList.getPatientExtended(patient.getId()) == null){ // save if patient does not exit in the patient extended record...
                    PatientExtended savedAdminList = administerList.savePatientExtended(patientExtended);
                }else{ // update if patient record already exist.
                    PatientExtended savedAdminList = administerList.updatePatientExtended(patientExtended);
                }*/



                log.info("Patient record for: \""+patient.getGivenName()+" "+patient.getFamilyName()+"\" successfully edited by "+Context.getAuthenticatedUser().getDisplayString());



                InfoErrorMessageUtil.flashInfoMessage(request.getSession(),
                        ui.message("registrationapp.editContactInfoMessage.success", patient.getPersonName()));

                return "redirect:" + returnUrl;
            }
            catch (Exception e) {
                log.warn("Error occurred while saving patient's contact info", e);
                session.setAttribute(UiCommonsConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE, "registrationapp.save.fail");

            }

        } else {
            model.addAttribute("errors", errors);
            StringBuffer errorMessage = new StringBuffer(messageSourceService.getMessage("error.failed.validation"));
            errorMessage.append("<ul>");
            for (ObjectError error : errors.getAllErrors()) {
                errorMessage.append("<li>");
                errorMessage.append(messageSourceService.getMessage(error.getCode(), error.getArguments(),
                        error.getDefaultMessage(), null));
                errorMessage.append("</li>");
            }
            errorMessage.append("</ul>");
            session.setAttribute(UiCommonsConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE, errorMessage.toString());
        }

        addModelAttributes(model, patient, patientExtended,  formStructure.getSections().get(sectionId), administrationService, returnUrl,
                app);
        //redisplay the form
        return null;
    }


    private void addModelAttributes(PageModel model, Patient patient, PatientExtended patientExtended, Section section,
                                    AdministrationService adminService, String returnUrl,
                                    AppDescriptor app) throws Exception {


        // add hmo record...
        ManageHMOAdminLister adminLister = new ManageHMOAdminLister();
        List<HMO> allHMOs  = adminLister.getAllHMOs();



        model.addAttribute("hmos", allHMOs);


        model.addAttribute("app", app);
        model.addAttribute("returnUrl", returnUrl);
        model.put("uiUtils", new RegistrationAppUiUtils());
        model.addAttribute("patient", patient);
        model.addAttribute("patientextended", patientExtended);
        model.addAttribute("addressTemplate", AddressSupport.getInstance().getAddressTemplate().get(0));
        model.addAttribute("nameTemplate", NameSupport.getInstance().getDefaultLayoutTemplate());
        model.addAttribute("section", section);
        model.addAttribute("enableOverrideOfAddressPortlet",
                adminService.getGlobalProperty("addresshierarchy.enableOverrideOfAddressPortlet", "false"));
    }


}
