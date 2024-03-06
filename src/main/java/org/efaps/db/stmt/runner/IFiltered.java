package org.efaps.db.stmt.runner;

import org.efaps.db.stmt.filter.Filter;
import org.efaps.util.cache.CacheReloadException;

public interface IFiltered
{

    Filter getFilter()
        throws CacheReloadException;
}
