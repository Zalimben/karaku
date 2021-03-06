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
package py.una.pol.karaku.controller;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import py.una.pol.karaku.controller.reports.KarakuBaseReportController;
import py.una.pol.karaku.dao.restrictions.Where;
import py.una.pol.karaku.dao.search.ISearchParam;
import py.una.pol.karaku.util.I18nHelper;
import py.una.pol.karaku.util.LabelProvider;
import py.una.pol.karaku.util.Serializer;
import py.una.pol.karaku.util.StringUtils;

/**
 * 
 * @author Osmar Vianconi
 * @since 1.0
 * @version 1.0 02/08/2013
 * @deprecated usar {@link KarakuBaseReportController}
 */
@Deprecated
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class KarakuQueryController<T, K extends Serializable> extends
		KarakuAdvancedController<T, K> implements IKarakuQueryController<T, K> {

	private List<Object[]> values;

	/**
	 * Mantiene los filtros ingresados
	 */
	private Map<String, Object> filterOptions;
	/**
	 * Se utiliza para indicar el valor que debe ser visualizado en los filtros
	 * para los componentes de la vista, en caso de que sea necesario.
	 */
	private Map<String, LabelProvider> labels;
	/**
	 * Mantiene la lista de ordenes seleccionados
	 */
	private List<String> orderSelected;

	/**
	 * Genera la lista de ordenamiento disponible, solo es necesaria en algunos
	 * casos de uso, en los cuales debe ser sobreescrito y retornar la lista de
	 * ordenes disponibles.
	 */
	@Override
	public List<String> getBaseOrderOptions() {

		return Collections.emptyList();
	}

	@Override
	public abstract void generateQuery();

	@Override
	public Map<String, Object> getFilterQuery(Map<String, Object> paramsQuery) {

		StringBuilder sb = new StringBuilder();

		for (Entry<String, Object> entry : filterOptions.entrySet()) {
			if (StringUtils.isValid(entry.getValue())) {
				String value;
				if (labels.get(entry.getKey()) != null) {
					value = labels.get(entry.getKey()).getAsString(
							entry.getValue());

				} else {
					value = entry.getValue().toString();
					if ("true".equals(value)) {
						value = "SI";
					} else {
						if ("false".equals(value)) {
							value = "NO";
						}
					}
				}
				Serializer.contruct(sb, I18nHelper.getMessage(entry.getKey()),
						value.toUpperCase());
			}

		}
		paramsQuery.put("selectionFilters", sb.toString());
		return paramsQuery;

	}

	@Override
	public List<String> getOrderSelected() {

		return orderSelected;
	}

	@Override
	public void setOrderSelected(List<String> orderSelected) {

		this.orderSelected = orderSelected;
	}

	@Override
	public Map<String, Object> getFilterOptions() {

		if (filterOptions == null) {
			filterOptions = new LinkedHashMap<String, Object>();
		}
		return filterOptions;
	}

	@Override
	public void setFilterOptions(Map<String, Object> filterOptions) {

		this.filterOptions = filterOptions;
	}

	public Map<String, LabelProvider> getLabels() {

		if (labels == null) {
			labels = new LinkedHashMap<String, LabelProvider>();
		}
		return labels;
	}

	public void setLabels(Map<String, LabelProvider> labels) {

		this.labels = labels;
	}

	public LabelProvider setLabelProvider(String field, LabelProvider lp) {

		getLabels().put(field, lp);
		return labels.get(field);
	}

	@Override
	public String getDefaultPermission() {

		return "SIGH";
	}

	/**
	 * @return values
	 */
	public List<Object[]> getValues() {

		if (this.values == null) {
			this.loadValues();
		}
		return this.values;
	}

	/**
	 * @param values
	 *            values para setear
	 */
	public void setValues(List<Object[]> values) {

		this.values = values;
		getPagingHelper().udpateCount(Long.valueOf(values.size()));
	}

	/**
	 * Recarga las entidades. Este método es el que se encarga realmente de
	 * realizar las llamadas a la base de datos.
	 */
	protected void loadValues() {

		this.log.debug("Recargando entidades en el controlador {}", this);

		Where<T> baseWhere = this.getBaseWhere();

		this.getPagingHelper().udpateCount(
				this.getBaseLogic().getCount(baseWhere));

		ISearchParam sp = this.getPagingHelper().getISearchparam();
		this.configureSearchParam(sp);

		this.entities = this.getBaseLogic().getAll(baseWhere, sp);
	}
}
