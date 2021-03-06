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
package py.una.pol.karaku.services;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.core.GenericCollectionTypeResolver;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import py.una.pol.karaku.exception.KarakuRuntimeException;
import py.una.pol.karaku.replication.DTO;
import py.una.pol.karaku.replication.EntityNotFoundException;
import py.una.pol.karaku.replication.Shareable;

/**
 * {@link Converter} que utiliza reflexión.
 * 
 * <p>
 * Este converter es el converter por defecto de cualquier entidad, y su uso no
 * es muy recomendado ya que no implementa una lógica compleja y absolutamente
 * todo es compartido.
 * </p>
 * 
 * @author Arturo Volpe
 * @since 2.2.8
 * @version 1.0 Nov 12, 2013
 * 
 */
public abstract class ReflectionConverter<E extends Shareable, T extends DTO>
        implements Converter<E, T> {

    /**
	 * 
	 */
    private static final String RAWTYPES = "rawtypes";
    /**
	 * 
	 */
    private static final String UNCHECKED = "unchecked";
    private final Class<T> dtoClass;
    private final Class<E> entityClass;

    public ReflectionConverter(Class<T> dtoClass, Class<E> entityClass) {

        this.dtoClass = dtoClass;
        this.entityClass = entityClass;
    }

    @Override
    public Class<T> getDtoType() {

        return dtoClass;
    }

    @Override
    public Class<E> getEntityType() {

        return entityClass;
    }

    @Override
    @SuppressWarnings(UNCHECKED)
    public T toDTO(E entity, int depth) {

        return (T) map(entity, getDtoType(), depth, false);
    }

    @Override
    @SuppressWarnings(UNCHECKED)
    public E toEntity(T dto) {

        return (E) map(dto, getEntityType(), Integer.MAX_VALUE, true);
    }

    /**
     * 
     * @param source
     * @param targetClass
     * @param depth
     * @param dtoToEntity
     * @return
     */
    protected Object map(final Object source, final Class<?> targetClass,
            final int depth, final boolean dtoToEntity) {

        if (source == null) {
            return null;
        }
        try {
            final Object toRet = targetClass.newInstance();
            ReflectionUtils.doWithFields(source.getClass(),
                    new FieldCallback() {

                        @Override
                        public void doWith(Field field)
                                throws IllegalAccessException {

                            mapField(source, targetClass, depth, dtoToEntity,
                                    toRet, field);
                        }

                    }, ReflectionUtils.COPYABLE_FIELDS);
            return toRet;
        } catch (Exception e) {
            throw new KarakuRuntimeException(
                    String.format("Can't copy from %s to %s",
                            source.getClass(), targetClass), e);
        }
    }

    /**
     * @param source
     * @param targetClass
     * @param depth
     * @param dtoToEntity
     * @param bean
     * @param field
     * @throws IllegalAccessException
     */
    private void mapField(final Object source, final Class<?> targetClass,
            final int depth, final boolean dtoToEntity, final Object bean,
            Field field) throws IllegalAccessException {

        Field toSet = ReflectionUtils.findField(targetClass, field.getName());
        if (toSet == null) {
            return;
        }
        field.setAccessible(true);
        toSet.setAccessible(true);
        Class<?> s = field.getType();
        Class<?> t = toSet.getType();

        if (Collection.class.isAssignableFrom(s)
                && Collection.class.isAssignableFrom(t)) {
            mapCollection(source, depth, dtoToEntity, bean, field, toSet, t);
        } else if (s.isAssignableFrom(t)) {
            Object o = field.get(source);
            toSet.set(bean, o);
        } else if (checkMappable(s, t)) {
            Object o = convert(s, t, field.get(source), depth - 1, dtoToEntity);
            toSet.set(bean, o);
        } else {
            throw new KarakuRuntimeException(String.format(
                    "Cant copy from field %s to field %s", field, toSet));
        }
    }

    /**
     * @param source
     * @param target
     * @return
     */
    private boolean checkMappable(Class<?> source, Class<?> target) {

        return (Shareable.class.isAssignableFrom(source) && DTO.class
                .isAssignableFrom(target))
                || (Shareable.class.isAssignableFrom(target) && DTO.class
                        .isAssignableFrom(source));
    }

    /**
     * @param source
     * @param depth
     * @param dtoToEntity
     * @param bean
     * @param field
     * @param toSet
     * @param targetClass
     * @throws IllegalAccessException
     */
    @SuppressWarnings({ RAWTYPES, UNCHECKED })
    private void mapCollection(final Object source, final int depth,
            final boolean dtoToEntity, final Object bean, Field field,
            Field toSet, Class<?> targetClass) throws IllegalAccessException {

        if (depth > 0) {
            Collection colTarget = getNewCollection(targetClass);
            Collection colSource = (Collection) field.get(source);
            if (colSource == null) {
                return;
            }
            Class targetRowType = GenericCollectionTypeResolver
                    .getCollectionFieldType(toSet);
            for (Object row : colSource) {
                if (row instanceof Shareable) {
                    colTarget.add(convert(row.getClass(), targetRowType, row,
                            depth - 1, dtoToEntity));
                } else {
                    colTarget.add(map(row, targetRowType, depth - 1,
                            dtoToEntity));
                }
            }
            toSet.set(bean, colTarget);
        }
    }

    @SuppressWarnings({ UNCHECKED, RAWTYPES })
    private Object convert(Class fromClass, Class toClass, Object object,
            int depth, boolean dtoToEntity) {

        if (dtoToEntity) {
            try {
                return getConverter(toClass, fromClass).toEntity((DTO) object);
            } catch (EntityNotFoundException enf) {
                // TODO ver la forma de propagar
                throw new KarakuRuntimeException("Can't convert", enf);
            }
        }
        return getConverter(fromClass, toClass)
                .toDTO((Shareable) object, depth);
    }

    @SuppressWarnings(RAWTYPES)
    private Collection getNewCollection(Class collectionClass) {

        if (List.class.isAssignableFrom(collectionClass)) {
            return new ArrayList();
        }
        if (Set.class.isAssignableFrom(collectionClass)) {
            return new HashSet();
        }
        throw new KarakuRuntimeException("Unsupported type of collection "
                + collectionClass);
    }

    public abstract <Y extends Shareable, O extends DTO> Converter<Y, O> getConverter(
            Class<Y> entity, Class<O> dto);

    @Override
    public T beforeToDTO(E entity, int depth) {

        // TODO Auto-generated method stub
        return toDTO(entity, depth);
    }

}
