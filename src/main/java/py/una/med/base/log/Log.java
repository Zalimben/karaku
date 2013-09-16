/*
 * @Log.java 1.0 Sep 13, 2013 Sistema Integral de Gestion Hospitalaria
 */
package py.una.med.base.log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Anotaci n que sirve para indicar que se debe inyectar un {@link Logger}.
 * <p>
 * La utilizaci n debe ser como sigue:
 * 
 * <pre>
 * {@literal @}{@link Log}
 * {@link Logger} log;
 * </pre>
 * 
 * De esta forma, el {@link LogPostProcessor} se encargar  autom ticamente de
 * inyectar el {@link Logger} (a trav s del {@link LoggerFactory}) pertinente al
 * atributo.
 * </p>
 * <p>
 * Esto produce el mismo resultado que:
 * 
 * <pre>
 * {@link Logger} log = {@link LoggerFactory#getLogger(String)} //pasando como par metro el nombre de la clase.
 * </pre>
 * 
 * </p>
 * 
 * @author Arturo Volpe
 * @since 1.0
 * @version 1.0 Sep 13, 2013
 * @see LogPostProcessor
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Log {

	/**
	 * Nombre del log, si es <code>null</code>, entonces se utilizar  un
	 * {@link Logger} con el nombre del bean.
	 * 
	 * @return cadena que representa el nombre del {@link Logger},
	 *         <code>""</code> es interpretado como <code>null</code>.
	 */
	String name() default "";
}
