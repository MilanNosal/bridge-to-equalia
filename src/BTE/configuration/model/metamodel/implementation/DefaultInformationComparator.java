package BTE.configuration.model.metamodel.implementation;

import BTE.configuration.model.metamodel.interfaces.InformationComparator;
import BTE.configuration.model.model.interfaces.Information;

/**
 * Porovnava informacie podla cieloveho jazykoveho elementu. Kedze kontext
 * rodica predpokladam ekvivalentny, staci mi porovnavat hodnotu
 * targetElementValue.
 * @author Milan
 */
public class DefaultInformationComparator implements InformationComparator {
    public boolean compareInformations(Information firstInformation, Information secondInformation){
        // Ak nemaju urceny targetElement, budem sa tvarit ze su rovnake
        if(firstInformation.getTargetElementValue()==null && secondInformation.getTargetElementValue()==null){
            return true;
        }
        if(firstInformation.getTargetElementValue()==null && secondInformation.getTargetElementValue()!=null && secondInformation.getTargetElementValue().equals("")){
            return true;
        }
        if(secondInformation.getTargetElementValue()==null && firstInformation.getTargetElementValue()!=null && firstInformation.getTargetElementValue().equals("")){
            return true;
        }
        // Inak ak jedna nema urceny targetElement ale druha ano, tak rovnake nie su
        // (je pouzite alebo, pretoze vyuzivam kontext predchadzajucich porovnani)
        if(secondInformation.getTargetElementValue()==null || firstInformation.getTargetElementValue()==null){
            return false;
        }
        // A napokon ak maju rovnaky targetElement
        if(firstInformation.getTargetElementValue().equals(secondInformation.getTargetElementValue())){
            return true;
        }
        return false;
    }
}
