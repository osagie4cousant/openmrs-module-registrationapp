package org.openmrs.module.registrationapp.page.controller;


import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.EncounterService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.DataSetRowList;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.query.encounter.definition.AuditEncounterQuery;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FindPatientPageController {


    public void controller(UiSessionContext uiSessionContext,
                           PageModel model,
                           @RequestParam("appId") AppDescriptor app,
                           @SpringBean AllDefinitionLibraries libraries,
                           @SpringBean DataSetDefinitionService dsdService,
                           @SpringBean("encounterService") EncounterService encounterService
                           ) throws EvaluationException {

        Location currentLocation = uiSessionContext.getSessionLocation();

        List<EncounterType> encounterTypes = new ArrayList<EncounterType>();
        EncounterType encounterType = encounterService.getEncounterTypeByUuid(
                (app.getConfig().get("registrationEncounter").get("encounterType").getTextValue()));

        if (encounterType == null) {
            throw new IllegalStateException("No encounter type with uuid " + app.getConfig().get("encounterTypeUuid").getTextValue());
        }

        encounterTypes.add(encounterType);

        EncounterDataSetDefinition d = new EncounterDataSetDefinition();

        AuditEncounterQuery rowFilter = new AuditEncounterQuery();
        rowFilter.setEncounterTypes(encounterTypes); // You'll need to set this to a list containing the registration encounter type
        //retrieve only the registration encounters created in the previous 24 hours
        rowFilter.setCreatedOnOrAfter(new DateTime().minus(Days.ONE).toDate());
        d.addRowFilter(rowFilter, "");

        d.addColumn("patientId", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.patientId"), "");
        d.addColumn("encounterId", libraries.getDefinition(EncounterDataDefinition.class, "reporting.library.encounterDataDefinition.builtIn.encounterId"), "");
        d.addColumn("dateCreated", libraries.getDefinition(EncounterDataDefinition.class, "reporting.library.encounterDataDefinition.builtIn.dateCreated"), "");
        d.addColumn("encounterDatetime", libraries.getDefinition(EncounterDataDefinition.class, "reporting.library.encounterDataDefinition.builtIn.encounterDatetime"), "");

        d.addSortCriteria("dateCreated", SortCriteria.SortDirection.DESC);

        SimpleDataSet dataSet = (SimpleDataSet)dsdService.evaluate(d, new EvaluationContext());
        DataSetRowList rows = dataSet.getRows();
        List<Encounter> registrationEncounters = new ArrayList<Encounter>();
        //display the last 5 registration encounters for distinct patients
        Set<Integer> patientIds = new HashSet<Integer>();
        for (DataSetRow row : rows) {
            Integer patientId = (Integer)row.getColumnValue("patientId");
            Integer encounterId = (Integer)row.getColumnValue("encounterId");
            if (!patientIds.contains(patientId) ) {
                patientIds.add(patientId);
                if (registrationEncounters.size() < 5 ) {
                    registrationEncounters.add(encounterService.getEncounter(encounterId));
                }else {
                    break;
                }
            }

        }

        model.addAttribute("appEncounters", registrationEncounters);
        model.addAttribute("appId", app.getId());
    }
}
