package ru.softshaper.web.admin.view;

import java.util.Collection;

import ru.softshaper.web.admin.bean.obj.impl.FullObjectView;
import ru.softshaper.web.admin.bean.obj.impl.TitleObjectView;
import ru.softshaper.web.admin.bean.objlist.ListObjectsView;
import ru.softshaper.web.admin.bean.objlist.TableObjectsView;
import ru.softshaper.web.admin.view.params.ViewObjectsParams;

/**
 * Источник данных для формы
 *
 * @author asheknew
 *
 */
public interface DataSourceFromView {

  /**
   * получение идентификаторов объекта связаного через МкН
   *
   * @param contentCode код мета класса
   * @param multyfieldCode код МкН поля
   * @param id идентификатор текущего объекта
   * @return список ссылочных идентификаторов
   */
  Collection<String> getObjectsIdsByMultifield(String contentCode, String multyfieldCode, String id, boolean reverse);

  /**
   * @param contentCode
   * @param backLinkAttr
   * @param objId
   * @return
   */
  FullObjectView getNewObject(String contentCode, String backLinkAttr, String objId);

  /**
   * @param params
   * @return
   */
  TableObjectsView getTableObjects(final ViewObjectsParams params);

  /**
   * @param params
   * @return
   */
  ListObjectsView getListObjects(final ViewObjectsParams params);

  /**
   * @param params
   * @return
   */
  FullObjectView getFullObject(final ViewObjectsParams params);

  /**
   * @param params
   * @return
   */
  TitleObjectView getTitleObject(final ViewObjectsParams params);
}
