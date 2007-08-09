package org.efaps.webapp.wicket;

import org.apache.wicket.PageParameters;

import org.efaps.webapp.components.FormTable;
import org.efaps.webapp.models.EFapsApplicationSession;

public class WebForm extends ContentPage {

  private static final long serialVersionUID = -3554311414948286302L;

  public WebForm(PageParameters _parameters) throws Exception {
    
    EFapsApplicationSession session = (EFapsApplicationSession) getSession();
    super.setModel(session.getIFormModel(null));
    this.addComponents();
   
  }

  @Override
  protected void addComponents() throws Exception {

    super.addComponents();
    add(new FormTable("eFapsFormTable", super.getModel()));
  }

}
