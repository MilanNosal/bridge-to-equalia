package BTE.configuration.communication.gaastimpl.lang;

/**
 * Trieda reprezentujuca metodu z jazyka.
 * @author Milan
 */
public class Method extends ExecutableElement {
    private Object defaultValue;
    
    // <editor-fold defaultstate="collapsed" desc="Getters">
    public Object getDefaultValue() {
        return defaultValue;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Setters">
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Modifiers">

    public boolean isAbstract() {
        return ((this.getModifiers() & Modifier.ABSTRACT) > 0);
    }

    public boolean isFinal() {
        return ((this.getModifiers() & Modifier.FINAL) > 0);
    }

    public boolean isInterface() {
        return ((this.getModifiers() & Modifier.INTERFACE) > 0);
    }

    public boolean isNative() {
        return ((this.getModifiers() & Modifier.NATIVE) > 0);
    }

    public boolean isPrivate() {
        return ((this.getModifiers() & Modifier.PRIVATE) > 0);
    }

    public boolean isProtected() {
        return ((this.getModifiers() & Modifier.PROTECTED) > 0);
    }

    public boolean isPublic() {
        return ((this.getModifiers() & Modifier.PUBLIC) > 0);
    }

    public boolean isStatic() {
        return ((this.getModifiers() & Modifier.STATIC) > 0);
    }

    public boolean isStrict() {
        return ((this.getModifiers() & Modifier.STRICT) > 0);
    }

    public boolean isSynchronized() {
        return ((this.getModifiers() & Modifier.SYNCHRONIZED) > 0);
    }
    // </editor-fold>
}
