package ru.softshaper.web.view.bean;

import ru.softshaper.web.bean.FieldTypeView;

public class ViewSetting {

  private final String columnContent;
  private final Integer number;
  private final Boolean readonly;
  private final Boolean required;
  private final String title;
  private final Boolean titleField;
  private final Boolean tableField;
  private final FieldTypeView typeView;

  public ViewSetting(String columnContent, Integer number, Boolean readonly, Boolean required, String title, Boolean titleField, Boolean tableField, FieldTypeView typeView) {
    this.columnContent = columnContent;
    this.number = number;
    this.readonly = readonly;
    this.required = required;
    this.title = title;
    this.titleField = titleField;
    this.tableField = tableField;
    this.typeView = typeView;
  }

  /**
   * @return the columnContent
   */
  public String getColumnContent() {
    return columnContent;
  }

  /**
   * @return the number
   */
  public Integer getNumber() {
    return number;
  }

  /**
   * @return the readonly
   */
  public Boolean getReadonly() {
    return readonly;
  }

  /**
   * @return the required
   */
  public Boolean getRequired() {
    return required;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @return the titlefield
   */
  public Boolean isTitleField() {
    return titleField;
  }

  public Boolean isTableField() {
    return tableField;
  }

  /**
   * @return the typeView
   */
  public FieldTypeView getTypeView() {
    return typeView;
  }
}