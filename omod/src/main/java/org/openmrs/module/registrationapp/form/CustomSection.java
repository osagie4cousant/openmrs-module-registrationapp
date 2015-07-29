package org.openmrs.module.registrationapp.form;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.module.registrationapp.model.Field;
import org.openmrs.module.registrationapp.model.Question;
import org.openmrs.module.registrationapp.model.Section;
import org.openmrs.module.registrationapp.model.TextFieldWidget;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentRequest;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class creates custom sections for form structure
 * Created by Osagie Ehigiato <osagie.ehigiato@cousant.com> on 7/7/2015.
 */
public class CustomSection {
    protected final static Log log = LogFactory.getLog(CustomSection.class);
    public CustomSection(){}

    /**
     *
     * @return
     */
    public Section hMOSection(){
        Section hmoSection = new Section();
        Question q = new Question();
        Question insuranceQuestion = new Question();

        q.setId("addhmo");
        q.setHeader("Select HMO");
        q.setLegend("HMO");

        insuranceQuestion.setId("addinsurance");
        insuranceQuestion.setHeader("Patient's Insurer");
        insuranceQuestion.setLegend("Insurance Scheme");

        hmoSection.setId("insurance");
        hmoSection.setLabel("Insurance Details");
        hmoSection.addQuestion(q);
        //hmoSection.addQuestion(insuranceQuestion); insurance scheme section removed...

        return hmoSection;
    }//hMOSection


    /**
     *
     * @return
     */
    public Section createNextOfKinSection(){
        Section nOK = new Section();
        Question qNames = new Question();
        Question qContact = new Question();
        Question qRelationship = new Question();

        qNames.setId("namesquestion");
        qNames.setHeader("Names");
        qNames.setLegend("Next of Kin's Names");

        qContact.setId("contactquestion");
        qContact.setLegend("Contact");
        qContact.setHeader("Patient Next of Kin's Address");

        qRelationship.setId("relationshipquestion");
        qRelationship.setLegend("Relationship");
        qRelationship.setHeader("Next of Kin's Relationship With Patient");

        nOK.setId("nextofkin");
        nOK.setLabel("Next Of Kin");
        nOK.addQuestion(qNames);
        nOK.addQuestion(qContact);
        nOK.addQuestion(qRelationship);

        return nOK;
    }//createNextOfKinSection



}
