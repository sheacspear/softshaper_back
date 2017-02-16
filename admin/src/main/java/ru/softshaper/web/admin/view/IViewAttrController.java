package ru.softshaper.web.admin.view;

import ru.softshaper.services.meta.MetaField;
import ru.softshaper.services.meta.ObjectExtractor;
import ru.softshaper.web.admin.bean.obj.impl.ViewSetting;
import ru.softshaper.web.admin.bean.objlist.ListObjectsView;

/**
 *
 */
public interface IViewAttrController {

  /**
   * @param obj
   * @param metaField
   * @param fieldView
   * @return
   */
  <T> Object getValueByObject(T obj, MetaField metaField, ViewSetting fieldView, ObjectExtractor<T> objectExtractor);

  /**
   * @param obj
   * @param metaField
   * @param fieldView
   * @return
   */
  <T> Object getValueByTable(T obj, MetaField metaField, ViewSetting fieldView, ObjectExtractor<T> objectExtractor);

  /**
   * @param obj
   * @param metaField
   * @param fieldView
   * @return
   */
  <T> String getTitle(T obj, MetaField metaField, ViewSetting fieldView, ObjectExtractor<T> objectExtractor);

  /**
   * @param metaField
   * @return
   */
  <T> ListObjectsView getVariants(MetaField metaField, ObjectExtractor<T> objectExtractor);

}