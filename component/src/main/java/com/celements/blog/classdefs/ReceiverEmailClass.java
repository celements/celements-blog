package com.celements.blog.classdefs;

import java.util.List;

import org.springframework.stereotype.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.list.DisplayType;
import com.celements.model.classes.fields.list.StaticListField;

@Component(ReceiverEmailClass.CLASS_DEF_HINT)
public class ReceiverEmailClass extends AbstractClassDefinition implements BlogClassDefinition {

  public static final String DOC_NAME = "ReceiverEMail";
  public static final String SPACE = "Celements2";
  public static final String CLASS_DEF_HINT = SPACE + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE, DOC_NAME);

  public static final ClassField<String> FIELD_EMAIL = new StringField.Builder(
      CLASS_REF, "email")
          .prettyName("email")
          .size(30)
          .build();

  public static final ClassField<Boolean> FIELD_IS_ACTIVE = new BooleanField.Builder(
      CLASS_REF, "is_active")
          .prettyName("is_active")
          .build();

  public static final ClassField<List<String>> FIELD_ADDRESS_TYPE = new StaticListField.Builder(
      CLASS_REF, "address_type")
          .prettyName("address_type")
          .size(1)
          .multiSelect(false)
          .values(List.of("to", "cc", "bcc"))
          .displayType(DisplayType.select)
          .build();

  public ReceiverEmailClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

}
