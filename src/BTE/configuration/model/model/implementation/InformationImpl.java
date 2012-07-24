package BTE.configuration.model.model.implementation;

import BTE.configuration.communication.interfaces.AnnotationTypeInstance;
import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.model.interfaces.Information;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Zakladna implementacia triedy zaobalujucej objekt informacie.
 * @author Milan
 */
public class InformationImpl implements Information {
    // Cele meno elementu kodu, ku ktoremu sa podla kontextu vztahuje tato informacia.
    private String targetQualifiedName;

    // Vyjadruje, aka cast nazvu target elementu ma byt pouzita.
    private String targetElementValue;

    // Nazov atributu/elementu ktory urcuje target.
    private final String targetElementName;

    // Hodnota, ktoru ma mat XML element/atribut.
    private final String value;

    // Odkaz na popis charakteru tejto informacie v metamodeli.
    private final ConfigurationType MMConfiguration;

    // Anotacia, z ktorej su informacie extrahovane.
    private AnnotationTypeInstance informationSource;

    // Objekt, z ktoreho sa taha informacia.
    private final Object sourceValue;

    // Rodic v modeli
    private Information parent;

    // Potomkovia informacie v modeli
    private Map<ConfigurationType, List<Information>> children = new HashMap<ConfigurationType, List<Information>>();

    /**
     * Zjednoduseny konstruktor, v pripade, ze hodnota informacie je bezpredmetna
     * a informacia nie je mapovana priamo na nejaky objekt v zdrojovych kodoch.
     * @param targetQualifiedName
     * @param targetElementValue
     * @param targetElementName
     * @param value
     * @param MMConfiguration
     */
    public InformationImpl(String targetQualifiedName, String targetElementValue, String targetElementName, String value, ConfigurationType MMConfiguration){
        this(targetQualifiedName, targetElementValue, targetElementName, value, MMConfiguration, null, null);
    }

    /**
     * Konstruktor.
     * @param targetQualifiedName - Cele meno elementu kodu, ku ktoremu sa podla kontextu vztahuje tato informacia.
     * @param targetElementValue - Vyjadruje, aka cast nazvu target elementu ma byt pouzita.
     * @param targetElementName - Nazov atributu/elementu ktory urcuje target.
     * @param value - Hodnota, ktoru ma mat XML element/atribut.
     * @param MMConfiguration - Odkaz na popis charakteru tejto informacie v MM.
     * @param informationSource - Anotacia, z ktorej su informacie extrahovane.
     * @param sourceValue - Objekt, z ktoreho sa taha informacia.
     */
    public InformationImpl(String targetQualifiedName, String targetElementValue, String targetElementName, String value, ConfigurationType MMConfiguration, AnnotationTypeInstance informationSource, Object sourceValue) {
        this.targetQualifiedName = targetQualifiedName;
        this.targetElementValue = targetElementValue;
        this.targetElementName = targetElementName;
        this.value = value;
        this.MMConfiguration = MMConfiguration;
        this.informationSource = informationSource;
        this.sourceValue = sourceValue;
    }

    @Override
    public String getTargetQualifiedName() {
        return this.targetQualifiedName;
    }

    @Override
    public void setTargetQualifiedName(String targetQualifiedName){
        this.targetQualifiedName = targetQualifiedName;
    }

    @Override
    public String getTargetElementValue() {
        return targetElementValue;
    }

    @Override
    public void setTargetElementValue(String targetElementValue){
        this.targetElementValue = targetElementValue;
    }

    @Override
    public String getTargetElementName() {
        return targetElementName;
    }

    @Override
    public String getName() {
        return this.MMConfiguration.getMappingOfConfigurationToXML().getName();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public ConfigurationType getMMConfiguration() {
        return MMConfiguration;
    }

    @Override
    public AnnotationTypeInstance getInformationSource() {
        return informationSource;
    }

    @Override
    public void setInformationSource(AnnotationTypeInstance annotationInstance) {
        this.informationSource = annotationInstance;
    }

    @Override
    public Information getParent() {
        return parent;
    }

    @Override
    public void setParent(Information parent) {
        this.parent = parent;
    }

    @Override
    public Map<ConfigurationType, List<Information>> getChildren() {
        return children;
    }

    @Override
    public void setChildren(Map<ConfigurationType, List<Information>> children){
        this.children = children;
    }

    @Override
    public Object getSourceValue() {
        return sourceValue;
    }

    @Override
    public XMLProcessing getXMLProcessing() {
        return this.MMConfiguration.getMappingOfConfigurationToXML().getXMLOutputType();
    }

    @Override
    public XMLProcessing getTargetElementProcessing() {
        return this.MMConfiguration.getMappingOfTargetElement().getQNameOfTargetProcView();
    }

    @Override
    public void print(PrintStream ps, String offset) {
        if(offset==null) {
            offset = "";
        }
        
        // Samotny uzol
        ps.print(offset);
        ps.print("<");
        ps.print(this.getName());
        ps.print("[");
        ps.print(this.getXMLProcessing());
        ps.print("]-");
        ps.print(this.targetElementName);
        ps.print(":");
        ps.print(this.targetElementValue);
        ps.print(">");
        ps.println();
        
        // Jeho hodnota        
        if(this.value!=null) {
            ps.print(offset);
            ps.print("\t");
            ps.append(value);
            ps.println();
        }
        
        // A potomkovia
        for(ConfigurationType child : this.children.keySet()){
            for(Information inf : this.children.get(child)){
                inf.print(ps ,"\t"+offset);
            }
        }
        
        // Ukoncenie (XML-like notation)
        ps.print(offset);
        ps.print("</");
        ps.print(this.getName());
        ps.print(">");
        ps.println();
    }
}
