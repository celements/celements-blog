package com.celements.blog.classdefs;

import java.util.Date;

import org.springframework.stereotype.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.DateField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.number.IntField;

@Component(NewsletterConfigClass.CLASS_DEF_HINT)
public class NewsletterConfigClass extends AbstractClassDefinition implements BlogClassDefinition {

  public static final String DOC_NAME = "NewsletterConfigClass";
  public static final String SPACE = "Classes";
  public static final String CLASS_DEF_HINT = SPACE + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE, DOC_NAME);

  public static final ClassField<Integer> FIELD_TIMES_SENT = new IntField.Builder(
      CLASS_REF, "times_sent")
          .prettyName("times_sent")
          .size(30)
          .build();

  public static final ClassField<Date> FIELD_LAST_SENT_DATE = new DateField.Builder(
      CLASS_REF, "last_sent_date")
          .prettyName("last_sent_date")
          .emptyIsToday(0)
          .build();

  public static final ClassField<String> FIELD_LAST_SENDER = new StringField.Builder(
      CLASS_REF, "last_sender")
          .prettyName("last_sender")
          .size(30)
          .build();

  public static final ClassField<Integer> FIELD_LAST_SENT_RECIPIENTS = new IntField.Builder(
      CLASS_REF, "last_sent_recipients")
          .prettyName("last_sent_recipients")
          .size(30)
          .build();

  public static final ClassField<String> FIELD_FROM_ADDRESS = new StringField.Builder(
      CLASS_REF, "from_address")
          .prettyName("from_address")
          .size(30)
          .build();

  public static final ClassField<String> FIELD_REPLY_TO_ADDRESS = new StringField.Builder(
      CLASS_REF, "reply_to_address")
          .prettyName("reply_to_address")
          .size(30)
          .build();

  public static final ClassField<String> FIELD_SUBJECT = new StringField.Builder(
      CLASS_REF, "subject")
          .prettyName("subject")
          .size(30)
          .build();

  public NewsletterConfigClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

}
