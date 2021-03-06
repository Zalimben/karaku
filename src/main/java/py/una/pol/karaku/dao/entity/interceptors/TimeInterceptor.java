/*-
 * Copyright (c)
 *
 * 		2012-2014, Facultad Politécnica, Universidad Nacional de Asunción.
 * 		2012-2014, Facultad de Ciencias Médicas, Universidad Nacional de Asunción.
 * 		2012-2013, Centro Nacional de Computación, Universidad Nacional de Asunción.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package py.una.pol.karaku.dao.entity.interceptors;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import py.una.pol.karaku.dao.entity.Operation;
import py.una.pol.karaku.dao.entity.annotations.Time;

/**
 *
 * @author Arturo Volpe
 * @since 1.0
 * @version 1.0 Oct 1, 2013
 *
 */
@Component
public class TimeInterceptor extends AbstractInterceptor {

	/**
	 *
	 */
	private static final int FIRST_DAY_OF_YEAR = 1;
	/**
	 *
	 */
	private static final int FIRST_YEAR = 1970;

	@Override
	public Class<?>[] getObservedTypes() {

		return new Class<?>[] { Date.class };
	}

	@Override
	public Class<?>[] getObservedAnnotations() {

		return new Class<?>[] { Time.class, void.class };

	}

	@Override
	public boolean interceptable(Operation op, Field field, Object bean) {

		return op != Operation.DELETE;
	}

	@Override
	public void intercept(Operation op, Field f, Object bean) {

		Object o = ReflectionUtils.getField(f, bean);

		if (o == null) {
			return;
		}

		Time t = f.getAnnotation(Time.class);
		Date date = (Date) o;
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		if ((t == null) || t.type().equals(Time.Type.DATE)) {
			this.handleDate(c);
		} else if (t.type().equals(Time.Type.TIME)) {
			this.handleTime(c);
		}
		// DATETIME no es manejado por que no requeire ningun
		// trato especial

		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		ReflectionUtils.setField(f, bean, c.getTime());
	}

	/**
	 * @param c
	 */
	private void handleTime(Calendar c) {

		c.set(Calendar.YEAR, FIRST_YEAR);
		c.set(Calendar.DAY_OF_YEAR, FIRST_DAY_OF_YEAR);

	}

	/**
	 * @param c
	 */
	private void handleDate(Calendar c) {

		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
	}

}
