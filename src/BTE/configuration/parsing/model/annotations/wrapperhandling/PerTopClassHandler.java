package BTE.configuration.parsing.model.annotations.wrapperhandling;

import BTE.configuration.communication.interfaces.AnnotationTypeInstance;
import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.communication.scannotationscanner.AnnotationTypeInstanceImpl;
import BTE.configuration.model.metamodel.enums.TargetNameType;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.model.implementation.InformationImpl;
import BTE.configuration.model.model.interfaces.Information;
import BTE.configuration.parsing.model.annotations.ModelParsingUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * Trieda zapuzdrujuca generovanie wrapperov s politikou PER_TOP_CLASS.
 * Hlada najvnutornejsiu triedu, ktora zapuzdruje anotovany jazykovy element
 * a skusa porovnavat, ci nejde o spolocnu s inymi polozkami.
 * @author Milan
 */
public abstract class PerTopClassHandler {
    /**
     * Generuje wrappers pre top class, t.j. pre najvzdialenejsi uzol triedy v strome.
     * Podstatne jednoduchsie ako PER_CLASS.
     * @param information
     * @param metaconfigurationLoader
     * @return zoznam vygenerovanych wrapperov
     */
    protected static List<Information> generateWrappersPerTopClass(Information information, MetaConfigurationLoader metaconfigurationLoader){
        List<Information> wrappers = new ArrayList<Information>();

        List<Information> list = new ArrayList<Information>();
        
        // Kontext rodica wrapperov, jedine ak je kontext naozaj triedou
        Class context = null;
        if(ModelParsingUtilities.findJavaElementActualContext(information)!=null){
            switch(ModelParsingUtilities.findJavaElementActualContext(information).getJavaElementKind()){
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:{
                    context = ModelParsingUtilities.findJavaElementActualContext(information).getSourceClass();
                    break;
                }
                default:{
                }
            }
        }
        // Prechadzam deti
        for(ConfigurationType childConfiguration : information.getChildren().keySet()){
            for(Information child : information.getChildren().get(childConfiguration)){
                // Inicializacia cieloveho elementu na kontext
                String clazz = information.getParent().getTargetQualifiedName();
                // Najdem zaobalujucu triedu potomka
                Class targetClazz = ModelParsingUtilities.findTargetClass(child);
                if(targetClazz!=null){
                    // Ak nie je null
                    clazz = targetClazz.getName();
                }

                // Priznak, ci sa nasiel vhodny wrapper
                boolean found = false;
                for(Information wrapper : wrappers){
                    // Ak je wrapper vhodny
                    if(clazz.equals(wrapper.getTargetQualifiedName())) {
                        // Ak maju rovnake meno zapuzdrujucej triedy, ide o vhodny wrapper
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
                // Ak nenajde vhodny wrapper, musi vytvorit novy
                if(!found){
                    String value = ModelParsingUtilities.buildTargetElementValue(information.getParent(), information.getMMConfiguration(), null, clazz);

                    // Nazov mapovanej polozky cieloveho jazykoveho elementu
                    String name;
                    if(information.getMMConfiguration().getMappingOfTargetElement().getTargetNameType()==TargetNameType.GENERIC){
                        if(targetClazz!=null){
                            // Ak sme nasli triedu
                            name = metaconfigurationLoader.getElementKind(ModelParsingUtilities.determineClassType(targetClazz));
                        } else if (targetClazz==null && context!=null) {
                            // Ak trieda najdena nebola, a kontext je trieda
                            name = metaconfigurationLoader.getElementKind(ModelParsingUtilities.determineClassType(context));
                        } else {
                            // Ak trieda najdena nebola, ale kontext nema zaobalujucu triedu (cize ma iba balik)
                            AnnotationTypeInstance ai = ModelParsingUtilities.findJavaElementActualContext(information);
                            if(ai==null){
                                name = null;
                            } else {
                                name = metaconfigurationLoader.getElementKind(ai.getJavaElementKind());
                            }
                        }
                    } else {
                        name = information.getMMConfiguration().getMappingOfTargetElement().getTargetElementName();
                    }

                    AnnotationTypeInstance ai = null;
                    if(targetClazz!=null && !targetClazz.equals(context))
                    {
                        // Ak target class nie je null a nie je zhodna s kontextovou triedou
                        ai = new AnnotationTypeInstanceImpl(targetClazz, ModelParsingUtilities.determineClassType(targetClazz), null);
                    }
                    
                    Information wrapper = new InformationImpl(
                            clazz, value, name, null,
                            information.getMMConfiguration(), ai, null);

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

}
