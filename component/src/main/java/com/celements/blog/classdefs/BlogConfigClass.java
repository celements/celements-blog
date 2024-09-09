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
import com.celements.model.classes.fields.number.IntField;

@Component(BlogConfigClass.CLASS_DEF_HINT)
public class BlogConfigClass extends AbstractClassDefinition implements BlogClassDefinition {

  public static final String DOC_NAME = "BlogConfigClass";
  public static final String SPACE = "Celements2";
  public static final String CLASS_DEF_HINT = SPACE + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE, DOC_NAME);

  public static final ClassField<Boolean> FIELD_IS_SUBSCRIBABLE = new BooleanField.Builder(
      CLASS_REF, "is_subscribable")
          .prettyName("is_subscribable")
          .build();

  public static final ClassField<String> FIELD_SUBSCRIBE_TO = new StringField.Builder(
      CLASS_REF, "subscribe_to")
          .prettyName("subscribe_to")
          .size(30)
          .build();

  public static final ClassField<Integer> FIELD_ARTICLE_PER_PAGE = new IntField.Builder(
      CLASS_REF, "art_per_page")
          .prettyName("art_per_page")
          .size(5)
          .build();

  public static final ClassField<Boolean> FIELD_IS_NEWSLETTER = new BooleanField.Builder(
      CLASS_REF, "is_newsletter")
          .prettyName("is_newsletter")
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

  public static final ClassField<Boolean> FIELD_UNSSUBSCRIBE_INFO = new BooleanField.Builder(
      CLASS_REF, "unsubscribe_info")
          .prettyName("unsubscribe_info")
          .build();

  public static final ClassField<String> FIELD_TEMPLATE = new StringField.Builder(
      CLASS_REF, "template")
          .prettyName("template")
          .size(30)
          .build();

  public static final ClassField<List<String>> FIELD_BLOG_EDITOR = new StaticListField.Builder(
      CLASS_REF, "blogeditor")
          .prettyName("blogeditor")
          .size(1)
          .multiSelect(false)
          .values(List.of("plain", "wysiwyg"))
          .displayType(DisplayType.select)
          .build();

  public static final ClassField<String> FIELD_BLOGSPACE = new StringField.Builder(
      CLASS_REF, "blogspace")
          .prettyName("blogspace")
          .size(30)
          .build();

  public static final ClassField<List<String>> FIELD_VIEWTYPE = new StaticListField.Builder(
      CLASS_REF, "viewtype")
          .prettyName("viewtype")
          .size(1)
          .multiSelect(false)
          .values(List.of("title", "extract", "full"))
          .displayType(DisplayType.select)
          .build();

  public static final ClassField<Boolean> FIELD_HAS_COMMENTS = new BooleanField.Builder(
      CLASS_REF, "has_comments")
          .prettyName("has_comments")
          .build();

  public static final ClassField<Integer> FIELD_MAX_NUM_CHARS_FIELD = new IntField.Builder(
      CLASS_REF, "max_num_chars")
          .prettyName("max number of characters in extract")
          .size(5)
          .build();

  public BlogConfigClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

}
