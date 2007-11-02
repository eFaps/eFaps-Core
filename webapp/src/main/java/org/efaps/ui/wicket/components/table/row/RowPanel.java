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

package org.efaps.ui.wicket.components.table.row;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.efaps.ui.wicket.components.table.cell.CellPanel;
import org.efaps.ui.wicket.models.CellModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.TableModel.RowModel;

/**
 * @author jmox
 * @version $Id$
 */
public class RowPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public RowPanel(final String _id, final RowModel _model,
                  final TableModel _tablemodel, final boolean _updateListMenu) {
    super(_id, _model);
    int i = 0;
    final RepeatingView cellRepeater = new RepeatingView("cellRepeater");
    add(cellRepeater);

    if (_tablemodel.isShowCheckBoxes()) {
      final CellPanel cellpanel =
          new CellPanel(cellRepeater.newChildId(), _model.getOids());
      cellpanel.setOutputMarkupId(true);
      cellpanel.add(new SimpleAttributeModifier("class",
          "eFapsTableCheckBoxCell"));
      cellRepeater.add(cellpanel);
    }

    for (CellModel cellmodel : _model.getValues()) {
      i++;

      final CellPanel cellpanel =
          new CellPanel(cellRepeater.newChildId(), cellmodel, _updateListMenu,
              _tablemodel);
      cellpanel.setOutputMarkupId(true);
      cellpanel.add(new SimpleAttributeModifier("class", "eFapsTableCell"));

      int width = 100 / _model.getValues().size();
      if (i == _model.getValues().size()) {
        width = width - (1);
      } else {
        cellpanel.add(new SimpleAttributeModifier("style", "width:"
            + width
            + "%"));
      }

      cellRepeater.add(cellpanel);
    }

  }
}