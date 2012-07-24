package BTE.configuration.model.metamodel.enums;

/**
 * Charakter generovania elementov, ktore nemaju mapovanie na zdrojove kody.
 * Sluzi hlavne pri vytvarani wrapperoch, elementoch, ktore oddeluju rodica
 * od potomkov. Moze ist napr. jediny element pod rodica, ktory zdruzi jeho
 * potomkov pod seba. Alebo rozdelenie potomkov podla cieloveho jazykoveho elementu,
 * pricom mozu byt zgrupovane podla spolocneho balika a pod.
 * @author Milan
 */
public enum GeneratingPolicy {
    /**
     * Pre kazdeho potomka je vygenerovany novy rodic tohto typu, tymto sa
     * oddelia potomkovia od rodica.
     * Pr. pre potomkov typu ukulele sa vygeneruju elementy typu bla:
     * <bla>
     *     <ukulele>1</ukulele>
     * </bla>
     * <bla>
     *     <ukulele>2</ukulele>
     * </bla>
     * <bla>
     *     <ukulele>3</ukulele>
     * </bla>
     */
    PER_CHILD,

    /**
     * Iba jeden element pre jedneho rodica.
     * Pr. pre potomkov typu ukulele sa vygeneruje jeden rodicovsky element typu bla:
     * <bla>
     *     <ukulele>1</ukulele>
     *     <ukulele>2</ukulele>
     *     <ukulele>3</ukulele>
     * </bla>
     */
    PER_PARENT,

    /**
     * Sleduju a porovnavaju cielove jazykove elementy potomkov. Vytvori sa
     * osobitny element pre najviac vonkajsie spolocne triedy.
     */
    PER_CLASS,

    /**
     * Porovnavaju sa spolocne jazykove elementy potomkov a vytvori sa osobitny
     * element pre najvnutornejsiu spolocnu triedu.
     */
    PER_TOP_CLASS,

    /**
     * Porovnavaju sa spolocne jazykove elementy potomkov a vytvori sa osobitny
     * element pre najvnutornejsi spolocny balik.
     */
    PER_TOP_PACKAGE,

    /**
     * Porovnavaju sa spolocne jazykove elementy potomkov a vytvori sa osobitny
     * element pre najvnutornejsi spolocny cielovy element.
     */
    PER_TOP_TARGET
}
