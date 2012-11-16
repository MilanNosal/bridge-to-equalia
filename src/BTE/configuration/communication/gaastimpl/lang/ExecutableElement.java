package BTE.configuration.communication.gaastimpl.lang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Milan
 */
public abstract class ExecutableElement extends LanguageElement {
    
    private int modifiers;
    private Class declaringClass;
    private List<Parameter> parameters = new ArrayList<Parameter>();
    
    private Set<Class> exceptionTypes = new HashSet<Class>();
    private Class returnType;
    
    // <editor-fold defaultstate="collapsed" desc="Getters">
    public Class getDeclaringClass() {
        return declaringClass;
    }

    public Set<Class> getExceptionTypes() {
        return exceptionTypes;
    }

    public Class getReturnType() {
        return returnType;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Setters">
    public void setReturnType(Class returnType) {
        this.returnType = returnType;
    }

    public void setDeclaringClass(Class declaringClass) {
        this.declaringClass = declaringClass;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Modifiers">
    public int getModifiers() {
        return this.modifiers;
    }
    // </editor-fold>
}