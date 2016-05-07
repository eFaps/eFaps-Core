/*
 * Copyright 2003 - 2016 The eFaps Team
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
package org.efaps.admin.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.util.EFapsException;

/**
 * The Class Index.
 *
 * @author The eFaps Team
 */
public final class Index
{

    /**
     * Instantiates a new index.
     */
    private Index()
    {
    }

    /**
     * Gets the analyzer.
     *
     * @return the analyzer
     * @throws EFapsException on error
     */
    public static Analyzer getAnalyzer()
        throws EFapsException
    {
        IAnalyzerProvider provider = null;
        if (EFapsSystemConfiguration.get().containsAttributeValue(KernelSettings.INDEXANALYZERPROVCLASS)) {
            final String clazzname = EFapsSystemConfiguration.get().getAttributeValue(
                            KernelSettings.INDEXANALYZERPROVCLASS);
            try {
                final Class<?> clazz = Class.forName(clazzname, false, EFapsClassLoader.getInstance());
                provider = (IAnalyzerProvider) clazz.newInstance();
            } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new EFapsException(Index.class, "Could not instanciate IAnalyzerProvider", e);
            }
        } else {
            provider = new IAnalyzerProvider()
            {
                @Override
                public Analyzer getAnalyzer()
                {
                    return new StandardAnalyzer(SpanishAnalyzer.getDefaultStopSet());
                }
            };
        }
        return provider.getAnalyzer();
    }

    /**
     * Gets the directory.
     *
     * @return the directory
     * @throws EFapsException on error
     */
    public static Directory getDirectory()
        throws EFapsException
    {
        IDirectoryProvider provider = null;
        if (EFapsSystemConfiguration.get().containsAttributeValue(KernelSettings.INDEXDIRECTORYPROVCLASS)) {
            final String clazzname = EFapsSystemConfiguration.get().getAttributeValue(
                            KernelSettings.INDEXDIRECTORYPROVCLASS);
            try {
                final Class<?> clazz = Class.forName(clazzname, false, EFapsClassLoader.getInstance());
                provider = (IDirectoryProvider) clazz.newInstance();
            } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new EFapsException(Index.class, "Could not instanciate IDirectoryProvider", e);
            }
        } else {
            provider = new IDirectoryProvider()
            {
                @Override
                public Directory getDirectory()
                    throws EFapsException
                {
                    return new RAMDirectory();
                }
            };
        }
        return provider.getDirectory();
    }
}
