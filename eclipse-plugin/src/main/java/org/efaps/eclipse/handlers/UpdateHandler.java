/*
 * Copyright 2003-2008 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.eclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;
import org.efaps.eclipse.EfapsPlugin;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class UpdateHandler extends AbstractHandler {
  /**
   * The constructor.
   */
  public UpdateHandler() {
  }

  /**
   * the command has been executed, so extract extract the needed information
   * from the application context.
   */
  @Override
  public Object execute(final ExecutionEvent _event)
      throws ExecutionException
  {
    final IEditorPart activeEditor = HandlerUtil.getActiveEditor(_event);
    if (activeEditor != null)  {
      final IEditorInput input = activeEditor.getEditorInput();
      if (input instanceof IFileEditorInput)  {
        final IFile file = ((IFileEditorInput) input).getFile();
        final IPath filePath = file.getLocation();

        if (!EfapsPlugin.getDefault().update(filePath.toString()))  {
          EfapsPlugin.getDefault().showError(_event,
                                             getClass(),
                                             "execute.failed");
        }
      }
    }

    return null;
  }
}