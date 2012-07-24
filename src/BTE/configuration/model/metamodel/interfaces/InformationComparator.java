package BTE.configuration.model.metamodel.interfaces;

import BTE.configuration.model.model.interfaces.Information;

/**
 * Rozhranie porovnavajuce dva informacie, pravdu vracia metoda v pripade
 * ekvivalencie informacii. Sluzi pri spajani, ked potrebujeme identifikovat,
 * ktore informacie z roznych zdrojov ekvivalentne, a ktore nie.
 * @author Milan
 */
public interface InformationComparator {
    /**
     * Metoda na urcenie, ci ide o ekvivalentne informacie. Predpoklada sa,
     * ze maju ekvivalentneho rodica (teda kontext rodica sa neporovnava). Nemal
     * by sa porovnavat ani kontext z pohladu potomkov, to je mozne dosiahnut
     * dalsim volanim tejto metody na potomkov, ma byt teda co najviac
     * "lightweight".
     * @param annotation
     * @param xml
     * @return
     */
    boolean compareInformations(Information firstInformation, Information secondInformation);
}
