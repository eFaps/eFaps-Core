/*
 * Copyright 2003-2007 The eFaps Team
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

package org.efaps.webapp.pages;

import org.apache.wicket.IPageMap;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.model.IModel;

import org.efaps.webapp.components.FormContainer;
import org.efaps.webapp.components.footer.FooterPanel;
import org.efaps.webapp.components.menu.MenuPanel;
import org.efaps.webapp.components.modalwindow.ModalWindowContainer;
import org.efaps.webapp.components.titel.TitelPanel;
import org.efaps.webapp.models.AbstractModel;

/**
 * @author jmo
 * @version $Id$
 */
public abstract class ContentPage extends WebPage {

  private static final long serialVersionUID = -2374207555009145191L;

  private final ModalWindowContainer modalWindow;

  public ContentPage(final IModel _model) {
    this(_model, null);
  }

  public ContentPage(final IModel _model,
                     final ModalWindowContainer _modalWindow) {
    super(_model);
    this.modalWindow = _modalWindow;
  }

  public ContentPage(final IModel _model,
                     final ModalWindowContainer _modalWindow,
                     final IPageMap _pagemap) {
    super(_pagemap, _model);
    this.modalWindow = _modalWindow;
  }

  protected void addComponents(FormContainer _form) {
    try {
      add(new StyleSheetReference("css", getClass(),
          "contentpage/ContentPage.css"));
      AbstractModel model = (AbstractModel) super.getModel();
      add(new TitelPanel("titel", model.getTitle()));

      add(new MenuPanel("menu", model, _form));
      WebMarkupContainer footerpanel;
      if (model.isCreateMode() || model.isEditMode() || model.isSearchMode()) {
        footerpanel = new FooterPanel("footer", model, this.modalWindow, _form);
      } else {
        footerpanel = new WebMarkupContainer("footer");
        footerpanel.setVisible(false);
      }

      add(footerpanel);
    } catch (Exception e) {

      e.printStackTrace();
    }
  }
}
