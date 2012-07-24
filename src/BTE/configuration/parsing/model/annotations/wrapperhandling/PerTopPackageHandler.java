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
import javax.lang.model.element.ElementKind;

/**
 * Trieda zapuzdrujuca generovanie wrapperov s politikou PER_TOP_PACKAGE.
 * Hlada najvnutornejsi balik, ktory zapuzdruje anotovany jazykovy element
 * a skusa porovnavat, ci nejde o spolocny s inymi polozkami.
 * @author Milan
 */
public abstract class PerTopPackageHandler {
    /**
     * Generuje zaobalovace pre najvyssi package, tj. spolu sa davaju iba tie
     * informacie, ktore maju rovnaky package.
     * @param information
     * @param metaconfigurationLoader
     * @return zoznam vygenerovanych wrappers
     */
    protected static List<Information> generateWrappersPerTopPackage(Information information, MetaConfigurationLoader metaconfigurationLoader){
        List<Information> wrappers = new ArrayList<Information>();

        List<Information> list = new ArrayList<Information>();

        // Kontext rodica wrapperov, jedine ak je kontext naozaj balikom
        Package context = null;
        if(ModelParsingUtilities.findJavaElementActualContext(information)!=null){
            switch(ModelParsingUtilities.findJavaElementActualContext(information).getJavaElementKind()){
                case PACKAGE:{
                    context = ModelParsingUtilities.findJavaElementActualContext(information).getSourcePackage();
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
                String pack = information.getParent().getTargetQualifiedName();
                // Najdem zaobalujuci balik
                Package targetPack = ModelParsingUtilities.findTargetPackage(child);
                if(targetPack!=null){
                    pack = targetPack.getName();
                }
                // Priznak, ci sa nasiel vhodny wrapper
                boolean found = false;
                for(Information wrapper : wrappers){
                    // Ak je wrapper vhodny
                    if(pack.equals(wrapper.getTargetQualifiedName())) {
                        // Ak maju rovnake meno zapuzdrujuceho balika, ide o vhodny wrapper
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
                    String value = ModelParsingUtilities.buildTargetElementValue(information.getParent(), information.getMMConfiguration(), null, pack);

                    // Nazov mapovanej polozky cieloveho jazykoveho elementu
                    String name;
                    if(information.getMMConfiguration().getMappingOfTargetElement().getTargetNameType()==TargetNameType.GENERIC){
                        if(targetPack!=null || context!=null){
                            // Ak sme nasli balik alebo kontext je balikom
                            name = metaconfigurationLoader.getElementKind(ElementKind.PACKAGE);
                        } else {
                            // Ak balik najdeny nebol, ale kontext nie je zaobalujucim balikom
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
                    if(targetPack!=null && !targetPack.equals(context))
                    {
                        // Ak target pack nie je null a nie je zhodny s kontextovym balikom
                        ai = new AnnotationTypeInstanceImpl(targetPack, ElementKind.PACKAGE, null);
                    }
                    
                    Information wrapper = new InformationImpl(
                            pack, value, name, null,
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
