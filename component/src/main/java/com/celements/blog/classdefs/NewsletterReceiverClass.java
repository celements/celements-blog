package com.celements.blog.classdefs;

import org.springframework.stereotype.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;

@Component(NewsletterReceiverClass.CLASS_DEF_HINT)
public class NewsletterReceiverClass extends AbstractClassDefinition
    implements BlogClassDefinition {

  public static final String DOC_NAME = "NewsletterReceiverClass";
  public static final String SPACE = "Celements";
  public static final String CLASS_DEF_HINT = SPACE + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE, DOC_NAME);

  public static final ClassField<String> FIELD_EMAIL = new StringField.Builder(
      CLASS_REF, "email")
          .prettyName("E-Mail")
          .size(30)
          .build();

  public static final ClassField<String> FIELD_LANGUAGE = new StringField.Builder(
      CLASS_REF, "language")
          .prettyName("Language code (ISO 639-1)")
          .size(30)
          .build();

  public static final ClassField<Boolean> FIELD_ISACTIVE = new BooleanField.Builder(
      CLASS_REF, "isactive")
          .prettyName("Is Active")
          .build();

  public static final ClassField<String> FIELD_SUBSCRIBED = new StringField.Builder(
      CLASS_REF, "subscribed")
          .prettyName("Subscribed to Newsletter(s) - " + "separated by ','")
          .size(30)
          .build();

  public NewsletterReceiverClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

}
