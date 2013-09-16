/*
 * @LogPostProcessor.java 1.0 Sep 13, 2013 Sistema Integral de Gestion
 * Hospitalaria
 */
package py.una.med.base.log;

import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

/**
 * {@link BeanPostProcessor} encargado de inyectar los {@link Logger} a trav s
 * de la anotaci n {@link Log} a los beans.
 * 
 * @author Arturo Volpe
 * @since 1.0
 * @version 1.0 Sep 13, 2013
 * @see Log
 */
@Component
public class LogPostProcessor implements BeanPostProcessor {

	/**
	 * Esta implementaci n no realiza ning n cambio. <br/>
	 * {@inheritDoc}
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {

		return bean;
	}

	/**
	 * Inyecta en los atributos con la anotaci n {@link Log} el log
	 * correspondiente.
	 * 
	 * @return Busca todos los {@link Log} y autom ticamente los inserta en el
	 *         {@link Field}
	 * @param bean
	 *            bean a inyectar
	 * @param beanName
	 *            nombre del bean
	 * @throws BeansException
	 *             nunca.
	 */
	@Override
	public Object postProcessAfterInitialization(final Object bean,
			String beanName) throws BeansException {

		ReflectionUtils.doWithFields(bean.getClass(), new FieldCallback() {

			@Override
			public void doWith(Field field) throws IllegalArgumentException,
					IllegalAccessException {

				ReflectionUtils.makeAccessible(field);
				Log log = field.getAnnotation(Log.class);
				if (log != null) {
					Logger logger;
					if (log.name() == null || "".equals(log.name().trim())) {
						logger = LoggerFactory.getLogger(bean.getClass());
					} else {
						logger = LoggerFactory.getLogger(log.name().trim());
					}
					field.set(bean, logger);
				}
			}
		});
		return bean;
	}

}
