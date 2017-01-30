package com.celements.blog.plugin;

import java.util.Date;

public class EmailAddressDate implements Comparable<EmailAddressDate> {

  private String emailAdr;
  private Date changeDate;
  private String language;

  public EmailAddressDate(String emailAdr, Date changeDate, String language) {
    this.emailAdr = emailAdr;
    if (changeDate != null) {
      this.changeDate = changeDate;
    } else {
      this.changeDate = new Date();
    }
    this.language = language;
  }

  @Override
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

  public String getLanguage() {
    return language;
  }
}
