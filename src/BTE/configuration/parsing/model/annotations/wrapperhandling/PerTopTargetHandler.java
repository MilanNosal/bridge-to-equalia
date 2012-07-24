package BTE.configuration.parsing.model.annotations.wrapperhandling;

import BTE.configuration.communication.interfaces.AnnotationTypeInstance;
import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.communication.scannotationscanner.AnnotationTypeInstanceImpl;
import BTE.configuration.exceptions.ParsingException;
import BTE.configuration.model.metamodel.enums.TargetNameType;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.model.implementation.InformationImpl;
import BTE.configuration.model.model.interfaces.Information;
import BTE.configuration.parsing.model.annotations.ModelParsingUtilities;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ElementKind;

/**
 * Trieda zaobaluje metody pre spracovanie generovania wrapperov pre politiku
 * GeneratingPolicy.PER_TOP_TARGET.
 * @author Milan
 */
public abstract class PerTopTargetHandler {
    /**
     * Generovanie wrapperov pre najvyssi target.
     * @param information
     * @param metaconfigurationLoader
     * @return zoznam wrapperov
     */
    protected static List<Information> generateWrappersPerTopTarget(Information information, MetaConfigurationLoader metaconfigurationLoader){
        List<Information> wrappers = new ArrayList<Information>();

        List<Information> list = new ArrayList<Information>();
        
        // Prechadzam deti
        for(ConfigurationType childConfiguration : information.getChildren().keySet()){
            for(Information child : information.getChildren().get(childConfiguration)){
                // Najdem target
                AnnotationTypeInstance ai = ModelParsingUtilities.findJavaElementActualContext(child);
                String target = information.getParent().getTargetQualifiedName();
                if(ai!=null){
                    target=ai.getJavaElementCanonicalName();
                }
                boolean found = false;
                for(Information wrapper : wrappers){
                    // Ak je wrapper vhodny
                    if(target.equals(wrapper.getTargetQualifiedName())) {
                        if(!wrapper.getChildren().containsKey(childConfiguration)){
                            wrapper.getChildren().put(childConfiguration, list);
                            list = new ArrayList<Information>();
                        }
                        wrapper.getChildren().get(childConfiguration).add(child);
                        child.setParent(wrapper);
                        found = true;
                        break;
                    }
                }
                // Ak nenajde vhodny wrapper
                if(!found){
                    String value = ModelParsingUtilities.buildTargetElementValue(information.getParent(), information.getMMConfiguration(), null, target);

                    String name = (information.getMMConfiguration().getMappingOfTargetElement().getTargetNameType()==TargetNameType.GENERIC)?
                        metaconfigurationLoader.getElementKind((ai==null)?null:ai.getJavaElementKind()):
                        information.getMMConfiguration().getMappingOfTargetElement().getTargetElementName();

                    // Dummy annotationInstance
                    AnnotationTypeInstance ai2 = null;
                    if(ai!=null)
                        ai2 = generateAnnotationInstance(ai);

                    Information wrapper = new InformationImpl(target, value, name, null, information.getMMConfiguration(), ai2, null);

                    wrappers.add(wrapper);

                    list.add(child);
                    wrapper.getChildren().put(childConfiguration, list);
                    child.setParent(wrapper);
                    
                    list = new ArrayList<Information>();
                }
            }
        }
        return wrappers;
    }

    /**
     * Metoda generuje dummy annotationInstance kvoli udrzaniu kontextu, ktory
     * ma zodpovedat AnnotationInstance ai.
     * @param ai
     * @return
     */
    private static AnnotationTypeInstance generateAnnotationInstance(AnnotationTypeInstance ai){
        switch(ai.getJavaElementKind()){
            case ANNOTATION_TYPE:{
                return new AnnotationTypeInstanceImpl(ai.getSourceClass(), ElementKind.ANNOTATION_TYPE, null);
            }
            case CLASS:{
                return new AnnotationTypeInstanceImpl(ai.getSourceClass(), ElementKind.CLASS, null);
            }
            case INTERFACE:{
                return new AnnotationTypeInstanceImpl(ai.getSourceClass(), ElementKind.INTERFACE, null);
            }
            case ENUM:{
                return new AnnotationTypeInstanceImpl(ai.getSourceClass(), ElementKind.ENUM, null);
            }
            case CONSTRUCTOR:{
                return new AnnotationTypeInstanceImpl(ai.getSourceConstructor(), ElementKind.CONSTRUCTOR, null);
            }
            case METHOD:{
                return new AnnotationTypeInstanceImpl(ai.getSourceMethod(), ElementKind.METHOD, null);
            }
            case FIELD:{
                return new AnnotationTypeInstanceImpl(ai.getSourceField(), ElementKind.FIELD, null);
            }
            case PARAMETER:{
                return ai.getSourceMethod()==null?
                    new AnnotationTypeInstanceImpl(ai.getSourceConstructor(), ElementKind.PARAMETER, null):
                    new AnnotationTypeInstanceImpl(ai.getSourceMethod(), ElementKind.PARAMETER, null);
            }
            case PACKAGE:{
                return new AnnotationTypeInstanceImpl(ai.getSourcePackage(), ElementKind.PACKAGE, null);
            }
            default:{
                throw new ParsingException("WrapperHandler<generateAnnotationInstance()>::\n\t" +
                        "ERROR: I was not supposed to get here!");
            }
        }
    }
}
