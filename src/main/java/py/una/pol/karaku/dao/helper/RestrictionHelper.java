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
package py.una.pol.karaku.dao.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import py.una.pol.karaku.dao.restrictions.Where;
import py.una.pol.karaku.dao.where.Clause;
import py.una.pol.karaku.log.Log;
import py.una.pol.karaku.util.ListHelper;

/**
 * Clase que sirve para interceptar restricciones que se agregan a un query, por
 * ejemplo para agregar alias para joins entre columnas, característica no
 * soportada por Hibernate Criteria
 * 
 * @author Arturo Volpe
 * @version 1.1
 * @since 1.0 08/02/2013
 * 
 * @param <T>
 */
@Component
public class RestrictionHelper implements ApplicationContextAware {

	@Log
	private transient Logger log;

	private static Map<Class<?>, BaseClauseHelper<?>> helpers;

	private ApplicationContext applicationContext;

	private static void register(BaseClauseHelper<?> newHelper) {

		getHelpers().put(newHelper.getClassOfClause(), newHelper);
	}

	/**
	 * @return helpers
	 */
	private static Map<Class<?>, BaseClauseHelper<?>> getHelpers() {

		if (helpers == null) {
			helpers = new HashMap<Class<?>, BaseClauseHelper<?>>();
		}

		return helpers;
	}

	@SuppressWarnings("rawtypes")
	@PostConstruct
	void postConstruct() {

		Map<String, BaseClauseHelper> help = this.applicationContext
				.getBeansOfType(BaseClauseHelper.class);
		for (Entry<String, BaseClauseHelper> entry : help.entrySet()) {
			register(entry.getValue());
		}
	}

	/**
	 * Agrega todas las clauses a la {@link Criteria}.
	 * 
	 * <p>
	 * Utiliza todas las instancias de {@link BaseClauseHelper} para poder
	 * realizar consultas anidadas a todos los tipos de {@link Clause}.
	 * </p>
	 * 
	 * <p>
	 * Para agregar soporte a mas restricciones, véase
	 * {@link #register(BaseClauseHelper)}
	 * </p>
	 * 
	 * @param where
	 *            filtros que se desea aplicar
	 * @param criteria
	 *            consulta actual
	 * @param alias
	 *            lista acutal de todos los alias que ya fueron agregados.
	 * @return Criteria con los filtro aplicados
	 */
	@SuppressWarnings("deprecation")
	public Criteria applyClauses(@Nonnull final Criteria criteria,
			@Nonnull final Where<?> where,
			@Nonnull final Map<String, String> alias) {

		Map<String, String> aliaz = alias;

		if (!ListHelper.hasElements(where.getCriterions())
				&& !ListHelper.hasElements(where.getClauses())) {
			return criteria;
		}
		List<Criterion> criterions = this.getCriterions(where.getClauses(),
				criteria, aliaz);
		for (Criterion c : criterions) {
			criteria.add(c);
		}
		return criteria;
	}

	/**
	 * Retorna la lista de {@link Criterion}, sin realmente modificar la
	 * consulta (solamente los alias listados se agregan a la consulta).
	 * 
	 * @param clauses
	 *            lista de {@link Clause}, not null.
	 * @param criteria
	 *            criteria para agregar alias
	 * @param alias
	 *            mapa de alias actuales
	 * @return {@link List} de criteriones, nunca null.
	 */
	public List<Criterion> getCriterions(@Nonnull List<Clause> clauses,
			@Nonnull Criteria criteria, @Nonnull Map<String, String> alias) {

		List<Criterion> criterions = new ArrayList<Criterion>(clauses.size());
		for (Clause cr : clauses) {
			if (cr == null) {
				continue;
			}
			criterions.add(this.getCriterion(cr, criteria, alias));
		}
		return criterions;
	}

	/**
	 * Retorna el {@link Criterion}, sin realmente modificar la consulta
	 * (solamente los alias listados se agregan a la consulta).
	 * 
	 * @param clause
	 *            {@link Clause}, not null.
	 * @param criteria
	 *            criteria para agregar alias
	 * @param alias
	 *            mapa de alias actuales
	 * @return {@link List} de criteriones, nunca null.
	 */
	@SuppressWarnings("deprecation")
	public Criterion getCriterion(@Nonnull Clause clause,
			@Nonnull Criteria criteria, @Nonnull Map<String, String> alias) {

		BaseClauseHelper<?> helper = getHelpers().get(clause.getClass());

		if (helper == null) {
			this.log.warn("Helper not found for: {}", clause.getClass());
			return clause.getCriterion();
		} else {
			return getHelpers().get(clause.getClass()).getCriterion(criteria,
					clause, alias, true);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {

		this.applicationContext = applicationContext;
	}
}
