package com.celements.blog.classdefs;

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Component;

import com.celements.model.classes.AbstractLegacyClassPackage;
import com.celements.model.classes.ClassDefinition;

@Component
public class BlogClassPackage extends AbstractLegacyClassPackage {

  public static final String NAME = "blog";

  private final List<BlogClassDefinition> classDefs;

  @Inject
  public BlogClassPackage(ListableBeanFactory beanFactory) {
    this.classDefs = List.copyOf(beanFactory.getBeansOfType(BlogClassDefinition.class).values());
  }

  @Override
  public @NotEmpty String getName() {
    return NAME;
  }

  @Override
  public @NotNull List<? extends ClassDefinition> getClassDefinitions() {
    return classDefs;
  }

  @Override
  public @NotEmpty String getLegacyName() {
    return NAME;
  }

}
