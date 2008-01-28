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

package org.efaps.ui.wicket.components.footer;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.PageMap;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.util.string.JavascriptUtils;

import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.components.FileUploadListener;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.AbstractModel;
import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class UploadBehavior extends AbstractBehavior implements
    FileUploadListener {

  private static final long serialVersionUID = 1L;

  private Component component;

  private final ModalWindowContainer modalWindow;

  public UploadBehavior(final ModalWindowContainer _modalWindow) {
    this.modalWindow = _modalWindow;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.behavior.AbstractBehavior#bind(org.apache.wicket.Component)
   */
  @Override
  public void bind(final Component _form) {
    super.bind(_form);
    this.component = _form;
  }

  public void onSubmit() {
    try {
      executeEvents();
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    final FormModel model = (FormModel) this.component.getPage().getModel();
    String script;
    if (model.getTarget() == Target.MODAL) {

      script =
          JavascriptUtils.SCRIPT_OPEN_TAG
              + "  window.onload = function() {"
              + this.modalWindow.getReloadJavaScript()
              + ModalWindowContainer.getCloseJavacript()
              + "}"
              + JavascriptUtils.SCRIPT_CLOSE_TAG;

    } else {
      final AbstractModel openermodel =
          (AbstractModel) ((EFapsSession) Session.get()).getOpenerModel();
      Class<?> clazz;
      if (openermodel instanceof TableModel) {
        clazz = TablePage.class;
      } else {
        clazz = FormPage.class;
      }
      final CharSequence url =
          this.component.urlFor(PageMap.forName(MainPage.IFRAME_PAGEMAP_NAME),
              clazz, openermodel.getPageParameters());
      script =
          JavascriptUtils.SCRIPT_OPEN_TAG
              + "  window.onload = function() {"
              + " opener.location.href = '"
              + url
              + "'; self.close();"
              + "  top.window.close();}"
              + JavascriptUtils.SCRIPT_CLOSE_TAG;

    }
    this.component.getRequestCycle().getResponsePage().add(
        new StringHeaderContributor(script));
  }

  private boolean executeEvents() throws EFapsException {

    boolean ret = true;
    final List<Return> returns =
        ((AbstractModel) this.component.getParent().getModel())
            .executeEvents(null);
    for (final Return oneReturn : returns) {
      if (oneReturn.get(ReturnValues.TRUE) == null && !oneReturn.isEmpty()) {
        ret = false;
        break;
      }
    }
    return ret;
  }

}