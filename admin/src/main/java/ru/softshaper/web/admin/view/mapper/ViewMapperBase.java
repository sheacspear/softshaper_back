package ru.softshaper.web.admin.view.mapper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ru.softshaper.bean.meta.FieldTypeView;
import ru.softshaper.services.meta.FieldType;
import ru.softshaper.services.meta.MetaClass;
import ru.softshaper.services.meta.MetaField;
import ru.softshaper.services.meta.MetaStorage;
import ru.softshaper.services.meta.conditions.impl.ConditionFieldImpl;
import ru.softshaper.staticcontent.file.FileObjectStaticContent;
import ru.softshaper.web.admin.bean.obj.IObjectView;
import ru.softshaper.web.admin.bean.obj.builder.FullObjectViewBuilder;
import ru.softshaper.web.admin.bean.obj.impl.FullObjectView;
import ru.softshaper.web.admin.bean.obj.impl.TitleObjectView;
import ru.softshaper.web.admin.bean.objlist.ColumnView;
import ru.softshaper.web.admin.bean.objlist.ListObjectsView;
import ru.softshaper.web.admin.bean.objlist.ObjectRowView;
import ru.softshaper.web.admin.bean.objlist.TableObjectsView;
import ru.softshaper.web.admin.view.DataSourceFromView;
import ru.softshaper.web.admin.view.DataSourceFromViewStore;
import ru.softshaper.web.admin.view.DataViewMapper;
import ru.softshaper.web.admin.view.IViewSetting;
import ru.softshaper.web.admin.view.bean.ViewSetting;
import ru.softshaper.web.admin.view.impl.ViewSettingFactory;
import ru.softshaper.web.admin.view.utils.FieldCollection;
import ru.softshaper.web.admin.view.utils.ViewObjectParamsBuilder;
import ru.softshaper.web.admin.view.utils.ViewObjectsParams;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Базовый маппер
 */
public abstract class ViewMapperBase<T> implements DataViewMapper<T> {

  /**
   * Хранилище, которое возвращает представление поля по его параметрам (табица
   * и колонка)
   */
  private final ViewSettingFactory viewSetting;

  /**
   * MetaStorage
   */
  private final MetaStorage metaStorage;

  /**
   * Хранилище Источник данных для формы
   */
  private final DataSourceFromViewStore dataSourceFromViewStore;

  public ViewMapperBase(ViewSettingFactory viewSetting, MetaStorage metaStorage,
      DataSourceFromViewStore dataSourceFromViewStore) {
    this.viewSetting = viewSetting;
    this.metaStorage = metaStorage;
    this.dataSourceFromViewStore = dataSourceFromViewStore;
  }

  protected abstract String getId(T obj, MetaClass metaClass);

  protected abstract Object getValue(T obj, MetaField field);

  protected FullObjectView convertFullObject(T obj, MetaClass metaClass) {
    Map<ViewSetting, String> titleFields = Maps.newHashMap();
    FullObjectViewBuilder view = FullObjectView.newBuilder(metaClass.getCode(), getId(obj, metaClass));
    for (MetaField metaField : metaClass.getFields()) {
      ListObjectsView variants = null;
      ViewSetting fieldView = viewSetting.getView(metaField);
      FieldType fieldType = metaField.getType();
      Object value = null;
      if (FieldType.UNIVERSAL_LINK.equals(fieldType)) {
        value = getValue(obj, metaField);
        if (value != null) {
          String stringValue = value.toString();
          int delimiterPosition = stringValue.lastIndexOf("@");
          if (delimiterPosition > 0) {

            String identifier = stringValue.substring(0, delimiterPosition);
            String linkedClassCode = stringValue.substring(delimiterPosition + 1);
            MetaClass linkedClass = metaStorage.getMetaClass(linkedClassCode);
            if (linkedClass != null) {
              DataSourceFromView dataSourceFromView = dataSourceFromViewStore.get(linkedClass.getCode());
              if (FieldTypeView.LINK_INNER_OBJECT.equals(fieldView.getTypeView())) {
                ViewObjectsParams params = ViewObjectsParams.newBuilder(linkedClass).ids().add(identifier)
                    .setFieldCollection(FieldCollection.ALL).build();
                value = dataSourceFromView.getFullObject(params);
              } else if (FieldTypeView.LINK_BROWSE.equals(fieldView.getTypeView())) {
                ViewObjectsParams params = ViewObjectsParams.newBuilder(linkedClass).ids().add(identifier)
                    .setFieldCollection(FieldCollection.TITLE).build();
                value = dataSourceFromView.getTitleObject(params);
              }
            }
          }

        }
      } else if (FieldType.LINK.equals(fieldType)) {
        IObjectView valueLink = getLinkedValue(obj, metaField, FieldCollection.TITLE);
        if (FieldTypeView.LINK_SELECTBOX.equals(fieldView.getTypeView()) && metaField.getLinkToMetaClass() != null) {
          DataSourceFromView dataSourceFromView = dataSourceFromViewStore.get(metaField.getLinkToMetaClass().getCode());
          variants = dataSourceFromView.getListObjects(ViewObjectsParams.newBuilder(metaField.getLinkToMetaClass())
              .setFieldCollection(FieldCollection.TITLE).build());
          value = valueLink == null ? null : valueLink.getId();
        } else {
          value = valueLink;
        }
      } else if (FieldType.BACK_REFERENCE.equals(fieldType)) {
        if (metaField.getBackReferenceField().getType().equals(FieldType.MULTILINK)) {
          DataSourceFromView dataSourceFrom = dataSourceFromViewStore.get(metaField.getOwner().getCode());
          DataSourceFromView dataSourceTo = dataSourceFromViewStore.get(metaField.getLinkToMetaClass().getCode());
          Collection<String> objectsIds = dataSourceTo.getObjectsIdsByMultifield(metaClass.getCode(),
              metaField.getBackReferenceField().getCode(), getId(obj, metaClass), true);
          if (objectsIds != null && !objectsIds.isEmpty()) {
            ViewObjectParamsBuilder paramsBuilder = ViewObjectsParams.newBuilder(metaField.getLinkToMetaClass())
                .addIds(objectsIds);
            if (FieldTypeView.BACK_REFERENCE_LIST.equals(fieldView.getTypeView())) {
              ViewObjectsParams params = paramsBuilder.setFieldCollection(FieldCollection.TITLE).build();
              ListObjectsView listObjectsView = objectsIds != null ? dataSourceFrom.getListObjects(params) : null;
              // if (listObjectsView != null) {
              // listObjectsView.setBackLinkAttr(metaField.getBackReferenceField().getCode());
              // }
              value = listObjectsView;
            } else {
              ViewObjectsParams params = paramsBuilder.setFieldCollection(FieldCollection.TABLE).build();
              TableObjectsView tableObjectsView = objectsIds != null ? dataSourceFrom.getTableObjects(params) : null;
              // if (tableObjectsView != null) {
              /// tableObjectsView.setBackLinkAttr(metaField.getBackReferenceField().getCode());
              // }
              value = tableObjectsView;
            }
          } else {
            if (FieldTypeView.BACK_REFERENCE_LIST.equals(fieldView.getTypeView())) {
              value = new ListObjectsView(metaField.getLinkToMetaClass().getCode(), 0, Collections.emptyList());
            }
            // else {
            // value = emptyTableObjectsView(metaField.getLinkToMetaClass(),
            // metaField.getBackReferenceField());
            // }
          }
        } else {
          DataSourceFromView dataSourceFromView = dataSourceFromViewStore.get(metaField.getLinkToMetaClass().getCode());
          ViewObjectParamsBuilder paramsBuilder = ViewObjectsParams.newBuilder(metaField.getLinkToMetaClass())
              .setCondition(new ConditionFieldImpl(metaField.getBackReferenceField()).equal(getId(obj, metaClass)));
          if (FieldTypeView.BACK_REFERENCE_LIST.equals(fieldView.getTypeView())) {
            ListObjectsView listObjects = dataSourceFromView
                .getListObjects(paramsBuilder.setFieldCollection(FieldCollection.TITLE).build());
            // listObjects.setBackLinkAttr(metaField.getBackReferenceField().getCode());
            value = listObjects;
          } else {
            value = dataSourceFromView.getTableObjects(paramsBuilder.setFieldCollection(FieldCollection.TABLE).build());
          }
        }
      } else if (FieldType.MULTILINK.equals(fieldType)) {
        DataSourceFromView dataSourceFrom = dataSourceFromViewStore.get(metaField.getOwner().getCode());
        DataSourceFromView dataSourceTo = dataSourceFromViewStore.get(metaField.getLinkToMetaClass().getCode());
        Collection<String> objectsIds = dataSourceFrom.getObjectsIdsByMultifield(metaClass.getCode(),
            metaField.getCode(), getId(obj, metaClass), false);
        ViewObjectParamsBuilder paramsBuilder = ViewObjectsParams.newBuilder(metaField.getLinkToMetaClass())
            .addIds(objectsIds);
        if (FieldTypeView.MULTILINK_CHECKBOX.equals(fieldView.getTypeView())) {
          ViewObjectsParams params = paramsBuilder.setFieldCollection(FieldCollection.TITLE).build();
          value = objectsIds != null && !objectsIds.isEmpty() ? dataSourceTo.getListObjects(params) : null;
        } else {
          ViewObjectsParams params = paramsBuilder.setFieldCollection(FieldCollection.TABLE).build();
          value = objectsIds != null && !objectsIds.isEmpty() ? dataSourceTo.getTableObjects(params)
              : emptyTableObjectsView(metaField.getLinkToMetaClass());
        }
      } else if (FieldType.FILE.equals(fieldType)) {
        value = getLinkedValue(obj, metaField, FieldCollection.TITLE);
      } else {
        value = getValue(obj, metaField);
      }
      if (fieldView.isTitleField()) {
        titleFields.put(fieldView, value == null ? "" : value.toString());
      }
      view.addField(metaField, fieldView, value, variants);
    }
    String title = constructTitle(titleFields);
    view.setTitle(title);
    return view.build();
  }

  private TableObjectsView emptyTableObjectsView(MetaClass metaClass) {
    return new TableObjectsView(metaClass.getCode(), 0, constructColumnsView(metaClass), Collections.emptyList());
  }

  private TitleObjectView convertTitleObject(T obj, MetaClass metaClass) {
    Map<ViewSetting, String> titleFields = Maps.newHashMap();
    for (MetaField metaField : metaClass.getFields()) {
      if (metaField.getType().equals(FieldType.MULTILINK) || metaField.getType().equals(FieldType.BACK_REFERENCE)
          || metaField.getType().equals(FieldType.FILE)) {
        continue;
      }
      ViewSetting fieldView = viewSetting.getView(metaField);
      if (fieldView.isTitleField()) {
        FieldType fieldType = metaField.getType();
        if (FieldType.LINK.equals(fieldType)) {
          IObjectView valueLink = getLinkedValue(obj, metaField, FieldCollection.TITLE);
          if (valueLink != null) {
            titleFields.put(fieldView, valueLink.getTitle());
          }
        } else {
          Object value = getValue(obj, metaField);
          if (value != null) {
            titleFields.put(fieldView, value.toString());
          }
        }
      }
    }
    String title = constructTitle(titleFields);
    if (title == null || title.isEmpty()) {
      title = getId(obj, metaClass);
    }
    return new TitleObjectView(metaClass.getCode(), getId(obj, metaClass), title);
  }

  private TableObjectsView convertTableObjectsView(Collection<T> objects, String metaClassCode, Integer total) {
    Preconditions.checkNotNull(metaClassCode);
    MetaClass metaClass = metaStorage.getMetaClass(metaClassCode);
    Preconditions.checkNotNull(metaClass);
    List<ColumnView> columnsView = constructColumnsView(metaClass);
    List<ObjectRowView> objectsView = Lists.newArrayList();

    // todo: за такое порно, не грех и посадить

    // todo: кого сажать тебя или меня?
    Map<MetaField, Map<ObjectRowView, String>> linkedValues = new HashMap<>();
    objects.forEach(obj -> {
      List<Object> data = Lists.newArrayList();
      data.add(getId(obj, metaClass));
      Map<MetaField, String> linkedValuesOfObject = new HashMap<>();
      metaClass.getFields().forEach(metaField -> {
        ViewSetting fieldView = viewSetting.getView(metaField);
        if (fieldView.isTableField()) {
          FieldType fieldType = metaField.getType();
          if (!fieldType.equals(FieldType.MULTILINK) && !fieldType.equals(FieldType.BACK_REFERENCE)) {
            Object value = getValue(obj, metaField);
            if (fieldType == FieldType.LINK && value != null) {
              linkedValuesOfObject.put(metaField, value.toString());
            } else if (fieldType == FieldType.UNIVERSAL_LINK) {
              if (value != null) {
                String stringValue = value.toString();
                int delimiterPosition = stringValue.lastIndexOf("@");
                if (delimiterPosition > 0) {
                  String identifier = stringValue.substring(0, delimiterPosition);
                  String linkedClassCode = stringValue.substring(delimiterPosition + 1);
                  MetaClass linkedClass = metaStorage.getMetaClass(linkedClassCode);
                  if (linkedClass != null) {
                    DataSourceFromView dataSourceFromView = dataSourceFromViewStore.get(linkedClass.getCode());

                    ViewObjectsParams params = ViewObjectsParams.newBuilder(linkedClass).ids().add(identifier)
                        .setFieldCollection(FieldCollection.TITLE).build();
                    TitleObjectView titleObject = dataSourceFromView.getTitleObject(params);
                    value = titleObject == null ? value : titleObject.getTitle();
                  }
                }
              }
            }
            data.add(value);
          } else {
            Object value = null;
            if (fieldType == FieldType.BACK_REFERENCE) {
              if (metaField.getBackReferenceField().getType() == FieldType.MULTILINK) {
                DataSourceFromView dataSourceFromView = dataSourceFromViewStore.get(metaClassCode);
                Collection<String> linkIds = dataSourceFromView.getObjectsIdsByMultifield(
                    metaField.getLinkToMetaClass().getCode(), metaField.getBackReferenceField().getCode(),
                    getId(obj, metaClass), true);
                if (linkIds != null) {
                  DataSourceFromView linkedStore = dataSourceFromViewStore
                      .get(metaField.getLinkToMetaClass().getCode());
                  value = linkedStore.getTitleObject(
                      ViewObjectsParams.newBuilder(metaField.getLinkToMetaClass()).addIds(linkIds).build());
                }
              } else {
                DataSourceFromView dataSourceFromView = dataSourceFromViewStore
                    .get(metaField.getLinkToMetaClass().getCode());
                ViewObjectParamsBuilder paramsBuilder = ViewObjectsParams.newBuilder(metaField.getLinkToMetaClass())
                    // todo: вот тут идентификатор надо приводить к реальному
                    // типу
                    .setCondition(
                        new ConditionFieldImpl(metaField.getBackReferenceField()).equal(getId(obj, metaClass)));
                value = dataSourceFromView
                    .getListObjects(paramsBuilder.setFieldCollection(FieldCollection.TITLE).build());
              }
            }
            if (fieldType == FieldType.MULTILINK) {
              DataSourceFromView dataSourceFromView = dataSourceFromViewStore.get(metaClassCode);
              Collection<String> linkIds = dataSourceFromView.getObjectsIdsByMultifield(metaClassCode,
                  metaField.getCode(), getId(obj, metaClass), false);
              if (linkIds != null) {
                DataSourceFromView linkedStore = dataSourceFromViewStore.get(metaField.getLinkToMetaClass().getCode());
                value = linkedStore.getTitleObject(
                    ViewObjectsParams.newBuilder(metaField.getLinkToMetaClass()).addIds(linkIds).build());
              }
            }
            data.add(value);
          }
        }
      });
      ObjectRowView objectRowView = new ObjectRowView(getId(obj, metaClass), data, null);
      for (Map.Entry<MetaField, String> entry : linkedValuesOfObject.entrySet()) {
        Map<ObjectRowView, String> objectRowViewStringMap = linkedValues.get(entry.getKey());
        if (objectRowViewStringMap == null) {
          objectRowViewStringMap = new HashMap<>();
          linkedValues.put(entry.getKey(), objectRowViewStringMap);
        }
        objectRowViewStringMap.put(objectRowView, entry.getValue());
      }
      objectsView.add(objectRowView);
    });
    for (MetaField field : linkedValues.keySet()) {
      Map<ObjectRowView, String> objectRowViewStringMap = linkedValues.get(field);
      DataSourceFromView dataSourceFromView = dataSourceFromViewStore.get(field.getLinkToMetaClass().getCode());
      ViewObjectsParams params = ViewObjectsParams.newBuilder(field.getLinkToMetaClass())
          .addIds(objectRowViewStringMap.values()).setFieldCollection(FieldCollection.TITLE).build();
      int columnIndex = 0;
      for (ColumnView columnView : columnsView) {
        if (columnView.getKey().equals(field.getCode())) {
          break;
        }
        columnIndex++;
      }
      ListObjectsView ListObjectView = dataSourceFromView.getListObjects(params);
      for (Map.Entry<ObjectRowView, String> rowLink : objectRowViewStringMap.entrySet()) {
        ObjectRowView row = rowLink.getKey();
        for (TitleObjectView objectView : ListObjectView.getObjects()) {
          if (row.getData().get(columnIndex) != null
              && objectView.getId().equals(row.getData().get(columnIndex).toString())) {
            row.getData().set(columnIndex, objectView.getTitle());
          }
        }
      }
    }
    return new TableObjectsView(metaClassCode, total != null ? total : objectsView.size(), columnsView, objectsView);
  }

  private List<ColumnView> constructColumnsView(MetaClass metaClass) {
    List<ColumnView> columnsView = Lists.newArrayList();
    columnsView.add(new ColumnView("Идентификатор", "id", FieldTypeView.STRING_SINGLE, false));

    metaClass.getFields().forEach(dynamicField -> {
      ViewSetting typeView = viewSetting.getView(dynamicField);
      if (typeView.isTableField()) {
        columnsView.add(new ColumnView(dynamicField.getName(), dynamicField.getCode(), typeView.getTypeView()));
      }
    });
    return columnsView;
  }

  private IObjectView getLinkedValue(T obj, MetaField metaField, FieldCollection fieldCollection) {
    Object linkedObjId = getValue(obj, metaField);
    if (linkedObjId == null) {
      return null;
    }
    MetaClass linkedMetaClass = metaField.getLinkToMetaClass();
    if (metaField.getType().equals(FieldType.FILE)) {
      linkedMetaClass = metaStorage.getMetaClass(FileObjectStaticContent.META_CLASS);
    }
    ViewObjectsParams params = ViewObjectsParams.newBuilder(linkedMetaClass).ids().add(linkedObjId.toString())
        .setFieldCollection(fieldCollection).build();
    if (FieldCollection.TITLE.equals(fieldCollection)) {
      return dataSourceFromViewStore.get(linkedMetaClass.getCode()).getTitleObject(params);
    } else {
      return dataSourceFromViewStore.get(linkedMetaClass.getCode()).getFullObject(params);
    }
  }

  private String constructTitle(Map<ViewSetting, String> titleFields) {
    return titleFields.keySet().stream().sorted((o1, o2) -> Integer.compare(o1.getNumber(), o2.getNumber()))
        .map(titleFields::get).reduce((s, s2) -> s.isEmpty() ? s2 : s + (s2.isEmpty() ? "" : " " + s2)).orElse("");
  }

  protected ViewSettingFactory getViewSetting() {
    return viewSetting;
  }

  protected MetaStorage getMetaStorage() {
    return metaStorage;
  }

  protected DataSourceFromViewStore getDataSourceFromViewStore() {
    return dataSourceFromViewStore;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * ru.softshaper.web.view.DataViewMapper#convertFullObject(java.lang.Object,
   * java.lang.String)
   */
  @Override
  public FullObjectView convertFullObject(T obj, String metaClassCode) {
    Preconditions.checkNotNull(metaClassCode);
    MetaClass metaClass = metaStorage.getMetaClass(metaClassCode);
    Preconditions.checkNotNull(metaClass);
    return convertFullObject(obj, metaClass);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * ru.softshaper.web.view.DataViewMapper#convertTitleObject(java.lang.Object,
   * java.lang.String)
   */
  @Override
  public TitleObjectView convertTitleObject(T obj, String metaClassCode) {
    Preconditions.checkNotNull(metaClassCode);
    MetaClass metaClass = metaStorage.getMetaClass(metaClassCode);
    Preconditions.checkNotNull(metaClass);
    return convertTitleObject(obj, metaClass);
  }

  /*
   * (non-Javadoc)
   * 
   * @see ru.softshaper.web.view.DataViewMapper#convertTableObjects(java.util.
   * Collection, java.lang.String, java.lang.Integer)
   */
  @Override
  public TableObjectsView convertTableObjects(Collection<T> objList, String metaClassCode, Integer total) {
    return convertTableObjectsView(objList, metaClassCode, total);
  }

  /*
   * (non-Javadoc)
   * 
   * @see ru.softshaper.web.view.DataViewMapper#convertListObjects(java.util.
   * Collection, java.lang.String, java.lang.Integer)
   */
  @Override
  public ListObjectsView convertListObjects(Collection<T> objects, String metaClassCode, Integer total) {
    Preconditions.checkNotNull(metaClassCode);
    MetaClass metaClass = metaStorage.getMetaClass(metaClassCode);
    Preconditions.checkNotNull(metaClass);
    List<TitleObjectView> titleObjects = objects.stream().map(record -> convertTitleObject(record, metaClass))
        .collect(Collectors.toList());
    return new ListObjectsView(metaClassCode, total, titleObjects);
  }

  /*
   * (non-Javadoc)
   * 
   * @see ru.softshaper.web.view.DataViewMapper#getEmptyObj(java.lang.String,
   * java.util.Map)
   */
  @Override
  public FullObjectView getEmptyObj(String contentCode, Map<String, Object> defValue) {
    Preconditions.checkNotNull(contentCode);
    MetaClass content = metaStorage.getMetaClass(contentCode);
    Preconditions.checkNotNull(content);
    FullObjectViewBuilder view = FullObjectView.newBuilder(contentCode, null);
    Preconditions.checkNotNull(content.getFields());
    for (MetaField metaField : content.getFields()) {
      IViewSetting fieldView = viewSetting.getView(metaField);
      if (fieldView.getTypeView().equals(FieldTypeView.LINK_SELECTBOX)) {
        view.addField(metaField, fieldView, defValue.get(metaField.getCode()),
            getVariants(metaField.getLinkToMetaClass()));
      } else {
        view.addField(metaField, fieldView, defValue.get(metaField.getCode()));
      }
    }
    return view.build();
  }

  private ListObjectsView getVariants(MetaClass metaClass) {
    DataSourceFromView dataSourceFromView = dataSourceFromViewStore.get(metaClass.getCode());
    return dataSourceFromView
        .getListObjects(ViewObjectsParams.newBuilder(metaClass).setFieldCollection(FieldCollection.TITLE).build());
  }

  @FunctionalInterface
  protected interface Extractor<O, V> {
    V value(O from);
  }
}