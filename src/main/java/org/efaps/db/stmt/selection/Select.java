/*
 * Copyright 2003 - 2018 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.efaps.db.stmt.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.collections4.multimap.AbstractListValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.efaps.db.Instance;
import org.efaps.db.stmt.selection.elements.AbstractElement;
import org.efaps.db.stmt.selection.elements.ISquash;
import org.efaps.util.EFapsException;

// TODO: Auto-generated Javadoc
/**
 * The Class Selection.
 *
 * @author The eFaps Team
 */
public final class Select
{

    /** The objects. */
    private final List<Object> objects = new ArrayList<>();

    /** The elements. */
    private final List<AbstractElement<?>> elements = new ArrayList<>();

    /** The alias. */
    private final String alias;

    /** The iterator. */
    private Iterator<Object> iterator;

    /** The current. */
    private Object current;

    /** The squash able. */
    private boolean noSquashRequired = true;

    /** The squash. */
    private Squashing squash;

    /**
     * Instantiates a new select.
     *
     * @param _alias the alias
     */
    private Select(final String _alias)
    {
        this.alias = _alias;
    }

    /**
     * Adds the element.
     *
     * @param _element the element
     * @return the select
     */
    protected Select addElement(final AbstractElement<?> _element)
    {
        this.noSquashRequired = this.noSquashRequired && !(_element instanceof ISquash);
        if (!this.elements.isEmpty()) {
            final AbstractElement<?> prev = this.elements.get(this.elements.size() - 1);
            _element.setPrevious(prev);
        }
        this.elements.add(_element);
        return this;
    }

    /**
     * Checks if is squash able.
     *
     * @return the squash able
     */
    protected boolean isSquash()
    {
        return !this.noSquashRequired;
    }

    protected Squashing getSquash()
    {
        return this.squash;
    }

    protected void setSquash(final Squashing _squash)
    {
        this.squash = _squash;
    }

    protected List<Object> getObjects() {
        return this.objects;
    }

    /**
     * Adds the object.
     *
     * @param _row the row
     * @throws EFapsException the e faps exception
     */
    public void addObject(final Object[] _row)
        throws EFapsException
    {
        this.objects.add(this.elements.get(0).getObject(_row));
    }

    /**
     * Gets the objects.
     *
     * @param _evaluator the evaluator
     * @return the objects
     * @throws EFapsException the e faps exception
     */
    protected List<Object> getObjects(final Evaluator _evaluator)
        throws EFapsException
    {
        final List<Object> result;
        if (_evaluator == null) {
            result = this.objects;
        } else {
            result = new ArrayList<>();
            final AbstractElement<?> element = getElements().get(getElements().size() - 1);
            final String path = element.getPath();
            final Select instSelection = _evaluator.getSelection().getInstSelects().get(StringUtils.isEmpty(path)
                            ? Selection.BASEPATH
                            : path);
            final List<Object> instObjs = instSelection.getObjects(null);
            final Iterator<Object> objIter = this.objects.iterator();
            for (final Object instObj : instObjs) {
                if (_evaluator.getAccess().hasAccess((Instance) instObj)) {
                    result.add(objIter.next());
                } else {
                    objIter.next();
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Gets the elements.
     *
     * @return the elements
     */
    public List<AbstractElement<?>> getElements()
    {
        return this.elements;
    }

    /**
     * Gets the alias.
     *
     * @return the alias
     */
    public String getAlias()
    {
        return this.alias;
    }

    /**
     * Gets the current.
     *
     * @return the current
     */
    public Object getCurrent()
    {
        return this.current instanceof ProxiedObject ? ((ProxiedObject) this.current).getObject() : this.current;
    }

    /**
     * Next.
     *
     * @return true, if successful
     */
    public boolean next()
    {
        boolean ret = false;
        if (this.iterator == null) {
            this.iterator = this.objects.iterator();
        }
        if (this.iterator.hasNext()) {
            this.current = this.iterator.next();
            ret = true;
        }
        return ret;
    }

    /**
     * Reset.
     */
    protected void reset()
    {
        this.iterator = null;
        this.current = null;
    }

    /**
     * Gets the.
     *
     * @return the select
     */
    public static Select get()
    {
        return new Select(null);
    }

    /**
     * Gets the.
     *
     * @param _alias the alias
     * @return the select
     */
    public static Select get(final String _alias)
    {
        return new Select(_alias);
    }

    /**
     * The Class SortedListValuedMap.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    public static class SortedListValuedMap<K, V>
        extends AbstractListValuedMap<K, V>
        implements Serializable
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new sorted list valued map.
         */
        public SortedListValuedMap()
        {
            super(new TreeMap<>());
        }

        @Override
        protected List<V> createCollection()
        {
            return new ArrayList<>();
        }
    }
}
