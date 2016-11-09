package ru.softshaper.web.bean.obj.attr;

import ru.softshaper.web.bean.obj.SettingFieldView;

/**
 * @author ashek
 *
 */
public class LinkClassSettingFieldView implements SettingFieldView {

  /**
   *
   */
  private final String linkMetaClass;

  public LinkClassSettingFieldView(String linkMetaClass) {
    super();
    this.linkMetaClass = linkMetaClass;
  }

  /**
   * @return the linkMetaClass
   */
  public String getLinkMetaClass() {
    return linkMetaClass;
  }

}