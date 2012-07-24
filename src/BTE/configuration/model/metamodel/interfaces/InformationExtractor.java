package BTE.configuration.model.metamodel.interfaces;

import BTE.configuration.model.model.interfaces.Information;

/**
 * Rozhranie pre pouzivatelom definovane hodnoty. Implementaciou rozhrania
 * je mozne pridavat dalsie sposoby extrakcie informacie zo zdrojovych kodov
 * (nie len informacie z anotacii ako takych, umoznuje vacsiu volnost)
 * a umoznuje vyssiu abstrakciu mapovania.
 * @author Milan
 */
public interface InformationExtractor {
    /**
     * Ma vratit hodnoty, ktore sa maju v danom pripade generovat. Pre kazdu hodnotu
     * je generovana jedna informacia. Viac hodnot v pripade, ze ma generovat
     * hodnoty napr. pre pole. Navratova hodnota nesmie byt null, ak generate
     * metoda vracia true (polozky pola mozu byt null, ak nechceme ziadny obsah
     * danej informacie). Objekt value nesie pomocnu informaciu, ak ide o anotaciu,
     * nesie mnozinu vhodnych AnnotationInstance, ak vlastnost, tak nesie objekt
     * vlastnosti. Inak null. Pri anotaciach musia byt navratove objekty typu
     * annotationInstance.
     * @param anchor
     * @param parent
     * @param configuration
     * @param value
     * @return
     */
    public Object[] getValues(Information anchor, Information parent, ConfigurationType configuration, Object value);

    /**
     * Touto metodou je mozne urcit, ci sa ma informacia v danom kontexte
     * generovat. Ak vrati hodnotu true, ma sa generovat, ak false, generovanie
     * sa vynecha. Objekt value nesie pomocnu informaciu, ak ide o anotaciu,
     * nesie prislusny objekt AnnotationInstance, ak vlastnost, tak nesie objekt
     * vlastnostou. Inak null.
     * @param anchor
     * @param parent
     * @param configuration
     * @param value
     * @return
     */
    public boolean generate(Information anchor, Information parent, ConfigurationType configuration, Object value);
}
