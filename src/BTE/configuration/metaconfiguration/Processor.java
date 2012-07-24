package BTE.configuration.metaconfiguration;

import BTE.configuration.model.metamodel.interfaces.ConfigurationType;

/**
 * Procesor pre modelovanie metamodelu.
 * @author Milan
 */
public interface Processor {
    /**
     * Navratova hodnota specifikuje spracovany metamodel, metoda moze
     * znehodnotit vstupny parameter, avsak musi vratit platny metamodel.
     * @param configuration
     * @return
     */
    public ConfigurationType process(ConfigurationType configuration);
}
