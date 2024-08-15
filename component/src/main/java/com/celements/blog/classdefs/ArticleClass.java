package com.celements.blog.classdefs;

import java.util.Date;

import org.springframework.stereotype.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.DateField;
import com.celements.model.classes.fields.LargeStringField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.number.IntField;
import com.xpn.xwiki.XWikiConstant;

@Component(ArticleClass.CLASS_DEF_HINT)
public class ArticleClass extends AbstractClassDefinition implements BlogClassDefinition {

  public static final String DOC_NAME = "ArticleClass";
  public static final String SPACE = XWikiConstant.XWIKI_SPACE;
  public static final String CLASS_DEF_HINT = SPACE + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE, DOC_NAME);

  public static final ClassField<String> FIELD_EXTRACT = new LargeStringField.Builder(
      CLASS_REF, "extract")
          .rows(15)
          .prettyName("extract")
          .size(80)
          .build();

  public static final ClassField<String> FIELD_TITLE = new LargeStringField.Builder(
      CLASS_REF, "title")
          .rows(15)
          .prettyName("title")
          .size(80)
          .build();

  public static final ClassField<String> FIELD_CONTENT = new LargeStringField.Builder(
      CLASS_REF, "content")
          .rows(15)
          .prettyName("content")
          .size(80)
          .build();

  public static final ClassField<Integer> FIELD_ID = new IntField.Builder(
      CLASS_REF, "id")
          .prettyName("id")
          .size(30)
          .build();

  public static final ClassField<String> FIELD_ARTICLE_LANG = new StringField.Builder(
      CLASS_REF, "lang")
          .prettyName("lang")
          .size(30)
          .build();

  public static final ClassField<String> FIELD_BLOGEDITOR = new StringField.Builder(
      CLASS_REF, "blogeditor")
          .prettyName("blogeditor")
          .size(30)
          .build();

  public static final ClassField<Date> FIELD_PUBLISH_DATE = new DateField.Builder(
      CLASS_REF, "publishdate")
          .prettyName("publishdate")
          .dateFormat("dd.MM.yyyy HH:mm")
          .size(0)
          .emptyIsToday(0)
          .validationRegExp(getRegexDate(false, true))
          .validationMessage("cel_blog_validation_publishdate")
          .build();

  public static final ClassField<Boolean> FIELD_HAS_COMMENTS = new BooleanField.Builder(
      CLASS_REF, "hasComments")
          .prettyName("hasComments")
          .displayType("yesno")
          .build();

  public static final ClassField<Date> FIELD_ARCHIVE_DATE = new DateField.Builder(
      CLASS_REF, "archivedate")
          .prettyName("archivedate")
          .dateFormat("dd.MM.yyyy HH:mm")
          .size(0)
          .emptyIsToday(0)
          .validationRegExp(getRegexDate(true, true))
          .validationMessage("cel_blog_validation_archivedate")
          .build();

  public static final ClassField<Boolean> FIELD_IS_SUBSCRIBABLE = new BooleanField.Builder(
      CLASS_REF, "isSubscribable")
          .prettyName("isSubscribable")
          .displayType("yesno")
          .build();

  public ArticleClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

  private static String getRegexDate(boolean allowEmpty, boolean withTime) {
    String regex = "(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.([0-9]{4})";
    if (withTime) {
      regex += " ([01][0-9]|2[0-4])(\\:[0-5][0-9])";
    }
    return "/" + (allowEmpty ? "(^$)|" : "") + "^(" + regex + ")$" + "/";
  }

}
