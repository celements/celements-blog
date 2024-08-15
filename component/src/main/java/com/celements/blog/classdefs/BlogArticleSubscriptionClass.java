package com.celements.blog.classdefs;

import org.springframework.stereotype.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;

@Component(BlogArticleSubscriptionClass.CLASS_DEF_HINT)
public class BlogArticleSubscriptionClass extends AbstractClassDefinition
    implements BlogClassDefinition {

  public static final String DOC_NAME = "BlogArticleSubscriptionClass";
  public static final String SPACE = "Celements2";
  public static final String CLASS_DEF_HINT = SPACE + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE, DOC_NAME);

  public static final ClassField<String> FIELD_SUBSCRIBER = new StringField.Builder(
      CLASS_REF, "subscriber")
          .prettyName("subscriber")
          .size(30)
          .build();

  public static final ClassField<Boolean> FIELD_DO_SUBSCRIBE = new BooleanField.Builder(
      CLASS_REF, "doSubscribe")
          .prettyName("doSubscribe")
          .displayType("yesno")
          .build();

  public BlogArticleSubscriptionClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

}
