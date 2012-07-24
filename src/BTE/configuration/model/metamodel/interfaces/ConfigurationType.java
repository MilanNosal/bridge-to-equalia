package BTE.configuration.model.metamodel.interfaces;

import BTE.configuration.model.metamodel.implementation.properties.MappingOfConfigurationToSources;
import BTE.configuration.model.metamodel.implementation.properties.MappingOfConfigurationToXML;
import BTE.configuration.model.metamodel.implementation.properties.MappingOfConfigurationToXSD;
import BTE.configuration.model.metamodel.implementation.properties.MappingOfTargetElement;
import java.io.PrintStream;
import java.util.List;

/**
 * Rozhranie pre typ konfiguracnej informacie. Zakladna stavebna jednotka
 * metamodelu.
 * @author Milan
 */
public interface ConfigurationType {
    /**
     * Objekt zapuzdrujuci politiku spracovania cieloveho jazykoveho elementu.
     * @return
     */
    MappingOfTargetElement getMappingOfTargetElement();

    /**
     * Objekt zapuzdrujuci informacie o mapovani informacie na XML a XSD.
     * @return
     */
    MappingOfConfigurationToXML getMappingOfConfigurationToXML();

    /**
     * Objekt zapuzdrujuci informacie o mapovani polozky na zdrojove text -
     * anotacie.
     * @return
     */
    MappingOfConfigurationToSources getMappingOfConfigurationToSources();

    /**
     * Objekt doplnujuci informacie o mapovani polozky na XSD.
     * @return
     */
    MappingOfConfigurationToXSD getMappingOfConfigurationToXSD();

    /**
     * Priznak, ci sa ma hladat identita v oboch formach konfiguracie (XML a
     * anotaciach) a vyberat prioritnejsia, pricom to co je len v jednej
     * forme, sa doplni. Ak je false, nahradia sa tieto informacie informaciami
     * z tej prioritonejsej formy. Ak v celom modely nie je true, poskytnu sa
     * iba informacie z jednej formy. Ak je napr anotacia oznacene ako true,
     * ale ziadny z jej nasledovnikov uz nie, ak sa najdu informacie z tej
     * istej anotacie (hladanie identit budem prevadzat na targetElement),
     * poskytne sa informacia z prioritnejsej. Pritom sa pouziju vsetci
     * nasledovnici danej informacie.
     * ! Napokon to obmedzim asi len na anotacie. Budem vsak dalej uvazovat
     * ! ze ako by sa to dalo rozumne spravit
     * // Nasledujuce je mozne dosiahnut iba upravou generovania schemy, pretoze
     * // schema nedovoluje defaultne, aby niektore decl. metody boli volitelne
     * // Alebo este mozem to urobit takto a ponechat tu upravu na visitorov,
     * // ktory budu nastavovat nieco take neskor.. proste aby sa postarali, ze
     * // "nadbytocne" informacie sa daju ako volitelne.. myslel som ze by som
     * // to mal robit ja..
     * Ak je napr. Table s true, a pod nou je
     * Column, ktoreho jedna decl. metoda je tiez true, tak sa informacie pod
     * Table nahradia prioritnejsimi, avsak ta deklarovana metoda bude spajana.
     * Ovsem ak nastane pripad, ze bude napr. cely column naviac vo forme s
     * nizsou prioritou, vo vysledku sa neobjavi cely column (tie informacie,
     * ktore sa nemaju spajat, sa zahodia), ale iba hodnota dekl. metody, ktora
     * bola oznacena pre spajanie(true). Ak by sme chceli aby sa ponechali
     * informacie z celeho column, oznacime takto samotny column.
     *
     * Ak nie je mozne rozoznat identitu na konfiguracie, ktora je takto
     * oznacena, proste sa spoja informacie z oboch zdrojov (ponechat pripadne
     * duplikacie informacii? napr dva elementy s rovnaky obsahom).
     *
     * Predvolene vracia false.
     * @return
     */
    boolean isMergingPoint();

    /**
     * Metoda pre nastavenie priznaku spajania.
     * @param mergingPoint
     */
    void setMergingPoint(boolean mergingPoint);

    /**
     * Metoda musi vediet identifikovat, ci ma dana polozka vo vetve nastaveny
     * bod spajania (vratane samotnej polozky).
     * @return
     */
    boolean isMergingPointInBranch();

    /**
     * Vrati implementaciu, ktora je schopna porovnat dve informacie tohto typu
     * a rozhodnut ci su ekvivalentne alebo nie (pre potreby spajania).
     * @return
     */
    InformationComparator getInformationComparator();

    /**
     * Metoda na nastavenie implementacie porovnavania informacii.
     * @param informationComparator
     */
    void setInformationComparator(InformationComparator informationComparator);

    /**
     * Metoda musi naklonovat danu polozku (typ konfiguracnej informacie) - urobit
     * jej kopiu. Pritom sa neberu do uvahy vztahy v hierarchii, iba informacie
     * o type konf. informacie.
     * @return
     */
    public ConfigurationType clone();

    /**
     * Rovnako ako clone(), avsak uz berie do uvahy aj vztahy smerom nadol -t.j.
     * naklonuje sa cela vetva.
     * @return
     */
    public ConfigurationType cloneBranch();

    /**
     * Vrati mi true, ak je dana polozka metamodelu kopiou - ziskana klonovanim.
     * @return
     */
    public boolean isCloned();

    /**
     * Metoda vrati skutocny zoznam potomkov danej polozky v hierarchii
     * metamodelu. Jeho modifikacia by sa mala prejavit modifikaciou hierarchie.
     * @return
     */
    List<ConfigurationType> getChildren();

    /**
     * Metoda, ktora prejde (napr. rekurzivne) vsetky dietky danej configuracie a skusi
     * najst tych potomkov, z ktorych by mali byt generovane vystupy. Tj ma
     * zistit (a najst) ci su medzi potomkami nejaky taki, ktori budu spracovani.
     * Pritom vracia zoznam potomkov (poloziek), ktore maju byt priamo
     * potomkami daneho typu konf. informacii. Pritom je dolezite poradie.
     * Pritom ak sa nejake polozky z potomkov maju preskocit, ale ich potomkovia
     * (vnuci aktualnej polozky) sa spracuvaju, tak ti sa vlozia v poradi
     * na miesto, kam patril ich rodic (takto je mozne urcit poradie aj pri
     * hlbsom preskakovani spracovania). Takto ma spravovat poradie tato metoda,
     * ak sa zmenia poradia potomkov, musia sa tieto zmeny prejavit aj v tejto
     * metode. Mala by vracat novy zoznam, t.j. ktoreho modifikaciou sa nezmeni
     * nic v hierarchii.
     * @return
     */
    List<ConfigurationType> getChildrenToProcess();

    /**
     * Metoda, ktorou je mozne nastavit zoznam potomkov.
     * @param children
     */
    void setChildren(List<ConfigurationType> children);

    /**
     * Rodic aktualnej polozky v hierarchii metamodelu, null znamena, ze rodica
     * nema (napr. koren).
     * @return
     */
    ConfigurationType getParent();

    /**
     * Metoda pre nastavenie rodica.
     * @param parent
     */
    void setParent(ConfigurationType parent);

    /**
     * Mnozina metainformacii o danej polozke, o danom type konfiguracnej
     * informacie. Cielom tejto mnoziny je spristupnit procesorom modelujucim
     * metamodel metainformacie, ktore mozu popisovat mapovanie konfiguracnych
     * informacii.
     * @return
     */
    List<Object> getMetainformations();

    /**
     * Metoda na nastavenie zoznamu metainformacii.
     * @param metainformations
     */
    void setMetainformations(List<Object> metainformations);
    
    /**
     * Metoda ma jednoduchu ulohu, vypisat rekurzivne model. Dolezite pre
     * pohodlny debugging.
     * @param ps Vystupny prud.
     * @param offset Pripadny offset vystupu, volitelny argument. POZOR!, moze byt null.
     */
    void print(PrintStream ps, String offset);
}
