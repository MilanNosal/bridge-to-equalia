package BTE.configuration.parsing.model.xml;

import BTE.configuration.communication.interfaces.MetaConfigurationLoader;
import BTE.configuration.model.metamodel.interfaces.ConfigurationType;
import BTE.configuration.model.model.interfaces.Information;
import java.io.InputStream;
import java.util.List;

/**
 * Parser, ktory sprostredkuje spracovanie viacerych dokumentov.
 * @author Milan
 */
public class MultiModelParser {
    /**
     * Metoda spracuje dokumenty ziskane prostrednictvom metakonfiguracie
     * podla metamodelu.
     * @param metaModel
     * @param loader
     * @return
     */
    public Information[] parseModels(ConfigurationType metaModel, MetaConfigurationLoader loader){
        List<InputStream> list = loader.getDocuments();
        Information[] result = new Information[list.size()];
        for(int i = 0; i < list.size(); i++){
            result[i] = (new XMLModelParser(metaModel, loader, list.get(i))).getModel();
        }
        return result;
    }

    /**
     * Metoda spracuje dokumenty, ktore su sprostredkovane parametrami streams.
     * @param metaModel
     * @param loader
     * @param streams
     * @return
     */
    public Information[] parseModels(ConfigurationType metaModel, MetaConfigurationLoader loader, InputStream... streams){
        Information[] result = new Information[streams.length];
        for(int i = 0; i < streams.length; i++){
            result[i] = (new XMLModelParser(metaModel, loader, streams[i])).getModel();
        }
        return result;
    }
}
