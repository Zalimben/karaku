/*
 * @HasRole.java 1.0 Mar 14, 2013 Sistema Integral de Gestión Hospitalaria
 */
package py.una.med.base.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import py.una.med.base.controller.AuthorityController;

/**
 * Determina si el usuario que se autentico al sistema tiene el permiso pasado
 * en {@link #value()}.
 * 
 * @see SIGHSecurity
 * @see AuthorityController
 * @author Arturo Volpe
 * @since 1.0
 * @version 1.0 Mar 14, 2013
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HasRole {

	/**
	 * Nombre del permiso que es requerido para ejecutar el método.
	 * 
	 * @see AuthorityController#hasRole(String)
	 * @return cadena válida que se utilizara para verificar el permiso.
	 */
	String value();
}
