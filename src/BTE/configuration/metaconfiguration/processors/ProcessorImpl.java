package BTE.configuration.metaconfiguration.processors;

import BTE.configuration.metaconfiguration.Processor;
import BTE.configuration.model.metamodel.implementation.ConfigurationTypeImpl;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;

/**
 * Nejaky "vseobecnejsi" procesor, v nom volam postupne vsetky konkretne
 * procesory.
 * @author Milan
 */
public class ProcessorImpl implements Processor {
    public ConfigurationType process(ConfigurationType configuration) {
        // Na poradi zavisi
        configuration = (new StaticModifierProcessor()).process(configuration);
        configuration = (new SkipProcessor()).process(configuration);
        configuration = (new MapsToProcessor()).process(configuration);
        configuration = (new InsideProcessor()).process(configuration);
        configuration = (new WrapperProcessor()).process(configuration);
        configuration = (new CopyBranchProcessor()).process(configuration);
        configuration = (new TargetElementProcessor()).process(configuration);
        configuration = (new AttributeProcessor()).process(configuration);
        return configuration;
    }
}
