package BTE.configuration.model.metamodel.implementation;

import BTE.configuration.model.metamodel.enums.XMLProcessing;
import BTE.configuration.model.metamodel.implementation.properties.MappingOfConfigurationToSources;
import BTE.configuration.model.metamodel.implementation.properties.MappingOfConfigurationToXML;
import BTE.configuration.model.metamodel.implementation.properties.MappingOfConfigurationToXSD;
import BTE.configuration.model.metamodel.implementation.properties.MappingOfTargetElement;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.metamodel.interfaces.InformationComparator;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Trieda predstavujuca typ konfiguracnej informacie. Hlavny stavebny prvok
 * metamodelu.
 */
public class ConfigurationTypeImpl implements ConfigurationType {
    // final - staci menit ich obsah
    // Mapovanie na XSD typ
    private final MappingOfConfigurationToXSD mappingOfConfigurationToXSD;

    // Mapovanie na zdrojove texty
    private final MappingOfConfigurationToSources mappingOfConfigurationToSources;

    // Mapovanie na XML
    private final MappingOfConfigurationToXML mappingOfConfigurationToXML;

    // Politika spracovania cieloveho jazykoveho elementu
    private final MappingOfTargetElement mappingOfTargetElement;

    // Premenne pre potreby spajania
    private boolean mergingPoint = false;

    // Objekt, ktory poskytuje metodu na porovnanie dvoch informacii na
    // ekvivalenciu
    private InformationComparator informationComparator = new DefaultInformationComparator();

    // Priznak, ci je polozka kopiou
    private boolean cloned = false;

    // **************  Hierarchia metamodelu **************
    /**
     * Rodic danej konfiguracie v stromovej hierarchie. Koren musi mat ako
     * rodica null hodnotu.
     */
    private ConfigurationType parent;

    /**
     * Potomkovia (nasledovnici) danej konfiguracie v hierarchii MM.
     */
    private List<ConfigurationType> children = new ArrayList<ConfigurationType>();

    /**
     * Metainformacie pre potreby procesorov na modelovanie metamodelu.
     */
    private List<Object> metainformations = new ArrayList<Object>();
    // *******************************************************************

    /**
     * Hlavny konstruktor. Pozor, pouziva predvolene hodnoty pre mergingPoint,
     * informationComparator a cloned premenne.
     * @param typeOfConfiguration typ v XSD
     * @param mappingOfConfigurationToSources zdroj informacii
     * @param mappingOfConfigurationToXML vzhlad mapovania do XML
     * @param mappingOfTargetElement spracovanie cieloveho elementu
     * @param parent
     */
    public ConfigurationTypeImpl(MappingOfConfigurationToXSD mappingOfConfigurationToXSD, MappingOfConfigurationToSources mappingOfConfigurationToSources, MappingOfConfigurationToXML mappingOfConfigurationToXML, MappingOfTargetElement mappingOfTargetElement, ConfigurationType parent) {
        this.mappingOfConfigurationToXSD = mappingOfConfigurationToXSD;
        this.mappingOfConfigurationToSources = mappingOfConfigurationToSources;
        this.mappingOfConfigurationToXML = mappingOfConfigurationToXML;
        this.mappingOfTargetElement = mappingOfTargetElement;
        this.parent = parent;
    }

    @Override
    public List<ConfigurationType> getChildren() {
        return children;
    }

    @Override
    public List<ConfigurationType> getChildrenToProcess(){
        // Rekurzivne hladanie potomkov
        List<ConfigurationType> list = new ArrayList<ConfigurationType>();
        for(ConfigurationType child : children){
            if(child.getMappingOfConfigurationToXML().getXMLOutputType() != XMLProcessing.SKIP_PROCESS){
                list.add(child);
            } else {
                list.addAll(child.getChildrenToProcess());
            }
        }
        return list;
    }

    @Override
    public void setChildren(List<ConfigurationType> children) {
        this.children = children;
    }

    @Override
    public List<Object> getMetainformations() {
        return metainformations;
    }

    @Override
    public void setMetainformations(List<Object> metainformations) {
        this.metainformations = metainformations;
    }

    @Override
    public ConfigurationType getParent() {
        return parent;
    }

    @Override
    public void setParent(ConfigurationType parent) {
        this.parent = parent;
    }

    @Override
    public MappingOfTargetElement getMappingOfTargetElement() {
        return mappingOfTargetElement;
    }

    @Override
    public MappingOfConfigurationToXML getMappingOfConfigurationToXML() {
        return mappingOfConfigurationToXML;
    }

    @Override
    public MappingOfConfigurationToSources getMappingOfConfigurationToSources() {
        return mappingOfConfigurationToSources;
    }

    @Override
    public MappingOfConfigurationToXSD getMappingOfConfigurationToXSD() {
        return mappingOfConfigurationToXSD;
    }

    @Override
    public boolean isMergingPoint() {
        return mergingPoint;
    }

    @Override
    public void setMergingPoint(boolean mergingPoint) {
        this.mergingPoint = mergingPoint;
    }
    
    @Override
    public boolean isMergingPointInBranch() {
        // Rekurzivne hladanie bodu spajania vo vetve
        if(this.mergingPoint){
            return true;
        }
        for(ConfigurationType child : children){
            if(child.isMergingPointInBranch()){
                return true;
            }
        }
        return false;
    }

    @Override
    public ConfigurationType clone() {
        // Pomerne primitivne klonovanie
        MappingOfConfigurationToSources source = new MappingOfConfigurationToSources(
                this.mappingOfConfigurationToSources.getConfAnnotation(), this.mappingOfConfigurationToSources.getQualifiedNameOfSource(),
                this.mappingOfConfigurationToSources.getSourceType(), this.mappingOfConfigurationToSources.getRelPositionToAnchor(), this.mappingOfConfigurationToSources.getPositionAnchor());
        source.setInformationExtractor(this.mappingOfConfigurationToSources.getInformationExtractor());

        MappingOfConfigurationToXSD type = new MappingOfConfigurationToXSD(this.mappingOfConfigurationToXSD.getTypeOfElement(), this.mappingOfConfigurationToXSD.getSimpleTypeValue());

        MappingOfConfigurationToXML view = new MappingOfConfigurationToXML
                (this.mappingOfConfigurationToXML.getName(), this.mappingOfConfigurationToXML.getTypeName(),
                this.mappingOfConfigurationToXML.getDefaultValue(),
                this.mappingOfConfigurationToXML.getXMLOutputType(), this.getMappingOfConfigurationToXML().getOrderPriority(),
                this.mappingOfConfigurationToXML.getMinOccurs(), this.mappingOfConfigurationToXML.getMaxOccurs());
        view.setGeneratingPolicy(this.mappingOfConfigurationToXML.getGeneratingPolicy());

        MappingOfTargetElement target = new MappingOfTargetElement
                (this.getMappingOfTargetElement().getQNameOfTargetProcType(), this.getMappingOfTargetElement().getQNameOfTargetProcView(),
                this.getMappingOfTargetElement().getTargetElementName(), this.getMappingOfTargetElement().getTargetNameType(),
                this.getMappingOfTargetElement().getTargetElements());

        ConfigurationTypeImpl configuration = new ConfigurationTypeImpl(type, source, view, target, null);
        // Nastavim priznak o klonovani
        configuration.cloned = true;
        configuration.setMergingPoint(mergingPoint);
        configuration.getMetainformations().addAll(metainformations);
        return configuration;
    }

    @Override
    public boolean isCloned() {
        return cloned;
    }

    @Override
    public ConfigurationType cloneBranch() {
        ConfigurationType clonedConf = this.clone();
        // Klonujem aktualny a rekurzivne aj jeho potomkov
        for(ConfigurationType child : this.children){
            ConfigurationType clonedChild = child.cloneBranch();
            clonedConf.getChildren().add(clonedChild);
            clonedChild.setParent(clonedConf);
        }
        return clonedConf;
    }

    @Override
    public InformationComparator getInformationComparator() {
        return informationComparator;
    }

    @Override
    public void setInformationComparator(InformationComparator informationComparator) {
        this.informationComparator = informationComparator;
    }

    /**
     * Prekrytie equals kvoli pouzitiu v tabulkach a mnozinach.
     * @param object
     * @return
     */
    @Override
    public boolean equals(Object object){
        if(!(object instanceof ConfigurationTypeImpl) || object == null ){
            return false;
        }
        // Porovnavam kvalifikovane meno mapovaneho zdroja
        if(!((ConfigurationTypeImpl)object).getMappingOfConfigurationToSources().getQualifiedNameOfSource().equals(this.getMappingOfConfigurationToSources().getQualifiedNameOfSource())){
            return false;
        }
        // Meno elementu/atributu, na ktory sa mapuje
        if(!((ConfigurationTypeImpl)object).getMappingOfConfigurationToXML().getName().equals(this.getMappingOfConfigurationToXML().getName())){
            return false;
        }
        // a meno typu elementu/atributu, na ktory sa mapuje
        if(!((ConfigurationTypeImpl)object).getMappingOfConfigurationToXML().getTypeName().equals(this.getMappingOfConfigurationToXML().getTypeName())){
            return false;
        }
        return true;
    }

    /**
     * Prekrytie hashCode() kvoli equals().
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + this.mappingOfConfigurationToSources.getQualifiedNameOfSource().hashCode();
        hash = 13 * hash + this.mappingOfConfigurationToXML.getName().hashCode();
        hash = 13 * hash + this.mappingOfConfigurationToXML.getTypeName().hashCode();
        return hash;
    }

    @Override
    public void print(PrintStream ps, String offset) {
        if(offset==null) {
            offset = "";
        }
        // Samotny uzol
        ps.print(offset);
        ps.print((this.mappingOfConfigurationToXML.getXMLOutputType()==XMLProcessing.ATTRIBUTE?"@":""));
        ps.print(this.mappingOfConfigurationToXML.getName());
        ps.print("[");
        ps.print(this.mappingOfConfigurationToSources.getQualifiedNameOfSource());
        ps.print("](");
        ps.print(this.getMappingOfTargetElement().getTargetElementName());
        ps.print("-");
        ps.print(this.getMappingOfTargetElement().getQNameOfTargetProcView());
        ps.print("-");
        ps.print(this.getMappingOfTargetElement().getQNameOfTargetProcType());
        ps.print("):");
        ps.println();
        ps.print(offset);
        ps.print("{");
        
        // Potomkovia
        boolean next = false;
        if(!children.isEmpty()){
            ps.println();
        }
        for(ConfigurationType conf : this.children){
            if(next==true) {
                ps.println();
            }
            ps.print(offset);
            conf.print(ps, offset+"\t");
            next = true;
        }
        if(!children.isEmpty()){
            ps.println();
        }
        
        // Ukoncenie uzla
        ps.print(offset);
        ps.print("}");
        ps.println();
    }
}
