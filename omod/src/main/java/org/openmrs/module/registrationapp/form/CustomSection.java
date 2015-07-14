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
        hmoSection.addQuestion(insuranceQuestion);

        return hmoSection;
    }//hMOSection


    /**
     *
     * @return
     */
    public Section createNextOfKinSection(){
        Section nOK = new Section();
        Question q = new Question();
        q.setId("addnextofkin");
        q.setHeader("Patient's Next of Kin Details");
        q.setLegend("Patient's Next of Kin");

        nOK.setId("nextofkin");
        nOK.setLabel("Next Of Kin");
        nOK.addQuestion(q);

        return nOK;
    }//createNextOfKinSection


    /**
     *
     * @param formfieldName
     * @param label
     * @param widget
     * @return

    private Field makeField(String formfieldName, String label, ObjectNode widget){

        //FragmentRequest fragmentRequest = new FragmentRequest();
        Field field = new Field();
        field.setFormFieldName(formfieldName);
        field.setLabel(label);
        field.setWidget(widget);
        //field.setFragmentRequest();


        log.error("New Field created: "+field.getLabel());
        return field;
    }//makeField*/

    /**
     * @param providername
     * @param fragmentId
     * @return
     *
    private ObjectNode makeWidget(String providername, String fragmentId){
        ObjectNode widget = JsonNodeFactory.instance.objectNode();
        log.error("New widget created! "+widget);
        widget.put("providerName", providername);
        widget.put("fragmentId", fragmentId);

        return widget;
    }//makeWidget */
}
