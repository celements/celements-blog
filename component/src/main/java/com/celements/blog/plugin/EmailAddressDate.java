package com.celements.blog.plugin;

import java.util.Date;

public class EmailAddressDate implements Comparable<EmailAddressDate>{

  private String emailAdr;
  private Date changeDate;

  public EmailAddressDate(String emailAdr, Date changeDate) {
    this.emailAdr = emailAdr;
    if (changeDate != null) {
      this.changeDate = changeDate;
    } else {
      this.changeDate = new Date();
    }
  }

  public int compareTo(EmailAddressDate emailAdrDate) {
    if (emailAdrDate.getChangeDate().compareTo(changeDate) != 0) {
      return emailAdrDate.getChangeDate().compareTo(changeDate);
    } else {
      return emailAdrDate.getEmailAdr().compareTo(emailAdr);
    }
  }

  public Date getChangeDate() {
    return changeDate;
  }

  public String getEmailAdr() {
    return emailAdr;
  }
}
