/*
 * @Bundle.java 1.0 Nov 7, 2013 Sistema Integral de Gestion Hospitalaria
 */
package py.una.med.base.replication.server;

import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * 
 * @author Arturo Volpe
 * @since 2.2.8
 * @version 1.0 Nov 7, 2013
 * 
 */
public class Bundle<T> implements Iterable<Change<T>> {

	/**
	 * Identificador utilizado cuando no hay cambios.
	 */
	public static final String ZERO_ID = "ZERO";
	private Deque<Change<T>> changes;

	/**
	 *
	 */
	public Bundle() {

		changes = new LinkedList<Change<T>>();
	}

	public String getLastId() {

		Change<T> c = changes.peekLast();

		return c == null ? ZERO_ID : c.getId();
	}

	/**
	 * Tamaño del cambio.
	 * 
	 * @return
	 */
	public int size() {

		return changes.size();
	}

	@Override
	public Iterator<Change<T>> iterator() {

		return changes.iterator();
	}

	public Change<T> add(T entity, String id) {

		Change<T> nC = new Change<T>();
		nC.setEntity(entity);
		nC.setId(id);
		changes.add(nC);
		return nC;
	}

	/**
	 * @return
	 */
	public Set<T> getEntities() {

		Set<T> set = new HashSet<T>(size());
		for (Change<T> c : this) {
			set.add(c.getEntity());
		}
		return set;
	}
}