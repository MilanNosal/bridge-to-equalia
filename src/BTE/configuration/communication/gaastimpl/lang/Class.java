package BTE.configuration.communication.gaastimpl.lang;

import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;

/**
 * Trieda objektov predstavujucich triedu v modele jazyka Java.
 *
 * @author Milan
 */
public class Class extends LanguageElement {

    /**
     *
     */
    private int modifiers;
    private Package pckage;
    private Class declaringClass;
    private String[] enumConstants;
    private Class componentType;
    private boolean primitive;
    private Set<Class> implementsClass = new HashSet<Class>();
    // TODO: zvazit aj zdedene metody (ale azda nebude treba)
    private Set<Method> declaredMethods = new HashSet<Method>();
    private Set<Constructor> declaredConstructors = new HashSet<Constructor>();
    private Set<Field> declaredFields = new HashSet<Field>();
    // TODO: dalsie
    // private final annotations

    public Class() {
    }
    
    // <editor-fold defaultstate="collapsed" desc="Instantiators">
    private Class(String name, String canonicalName, Package pckg,
            Class declaringClass, int modifiers, Class componentType,
            String[] enumConstants, boolean primitive) {
        this.modifiers = modifiers;
        super.setName(name);
        super.setCanonicalName(canonicalName);
        this.pckage = pckg;
        this.declaringClass = declaringClass;
        this.enumConstants = enumConstants;
        this.componentType = componentType;
        this.primitive = primitive;
    }

    public static Class getOrdinaryClass(String name, String canonicalName, Package pckg, int modifiers) {
        return new Class(name, canonicalName, pckg, null, modifiers, null, null, false);
    }

    public static Class getInnerClass(String name, String canonicalName, Package pckg, Class declaringClass, int modifiers) {
        return new Class(name, canonicalName, pckg, declaringClass, modifiers, null, null, false);
    }

    public static Class getPrimitive(String name, String canonicalName, Package pckg, int modifiers) {
        return new Class(name, canonicalName, pckg, null, modifiers, null, null, true);
    }

    public static Class getArray(String name, String canonicalName, Package pckg, Class componentType, int modifiers) {
        return new Class(name, canonicalName, pckg, null, modifiers, componentType, null, false);
    }

    public static Class getInnerEnum(String name, String canonicalName, Package pckg, String[] enumConstants, int modifiers) {
        return new Class(name, canonicalName, pckg, null, modifiers, null, enumConstants, false);
    }

    public static Class getInnerEnum(String name, String canonicalName, Package pckg, Class declaringClass, String[] enumConstants, int modifiers) {
        return new Class(name, canonicalName, pckg, declaringClass, modifiers, null, enumConstants, false);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Modifiers">
    public int getModifiers() {
        return this.modifiers;
    }

    public boolean isAbstract() {
        return ((this.modifiers & Modifier.ABSTRACT) > 0);
    }

    public boolean isFinal() {
        return ((this.modifiers & Modifier.FINAL) > 0);
    }

    public boolean isInterface() {
        return ((this.modifiers & Modifier.INTERFACE) > 0);
    }

    public boolean isPrivate() {
        return ((this.modifiers & Modifier.PRIVATE) > 0);
    }

    public boolean isProtected() {
        return ((this.modifiers & Modifier.PROTECTED) > 0);
    }

    public boolean isPublic() {
        return ((this.modifiers & Modifier.PUBLIC) > 0);
    }

    public boolean isStatic() {
        return ((this.modifiers & Modifier.STATIC) > 0);
    }

    public boolean isStrict() {
        return ((this.modifiers & Modifier.STRICT) > 0);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters">
    public Package getPackage() {
        return this.pckage;
    }

    public boolean isEnum() {
        return this.enumConstants == null ? false : true;
    }

    public String[] getEnumConstants() {
        return this.enumConstants;
    }

    public boolean isArray() {
        return this.componentType == null ? false : true;
    }

    public Class getComponentType() {
        return this.componentType;
    }

    public boolean isPrimitive() {
        return this.primitive;
    }

    public Class getDeclaringClass() {
        return this.declaringClass;
    }
    
    public Set<Class> getImplements() {
        return implementsClass;
    }

    public Set<Constructor> getDeclaredConstructors() {
        return declaredConstructors;
    }

    public Set<Field> getDeclaredFields() {
        return declaredFields;
    }

    public Set<Method> getDeclaredMethods() {
        return declaredMethods;
    }
    // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="Setters">
    public void setPackage(Package pckg) {
        this.pckage = pckg;
    }

    public void setComponentType(Class componentType) {
        this.componentType = componentType;
    }

    public void setDeclaringClass(Class declaringClass) {
        this.declaringClass = declaringClass;
    }

    public void setEnumConstants(String[] enumConstants) {
        this.enumConstants = enumConstants;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    public void setPrimitive(boolean primitive) {
        this.primitive = primitive;
    }
    // </editor-fold>
}
