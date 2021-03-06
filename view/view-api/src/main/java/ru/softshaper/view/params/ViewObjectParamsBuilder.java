package ru.softshaper.view.params;

import ru.softshaper.services.meta.MetaClass;
import ru.softshaper.services.meta.MetaField;
import ru.softshaper.services.meta.conditions.Condition;
import ru.softshaper.services.meta.impl.GetObjectsParams;
import ru.softshaper.services.meta.impl.GetObjectsParamsBuilder;
import ru.softshaper.services.meta.impl.SortOrder;

import java.util.Collection;

/**
 * Created by Sunchise on 13.03.2017.
 */
public class ViewObjectParamsBuilder {

  private final GetObjectsParamsBuilder getParamsBuilder;
  private FieldCollection fieldCollection = FieldCollection.ALL;

  public ViewObjectParamsBuilder(MetaClass metaClass) {
    getParamsBuilder = GetObjectsParams.newBuilder(metaClass);
  }

  public OrderFields orderFields() {
    return (field, order) -> {
      getParamsBuilder.orderFields().add(field, order);
      return ViewObjectParamsBuilder.this;
    };
  }

  public ViewObjectParamsBuilder setCondition(Condition condition) {
    getParamsBuilder.setCondition(condition);
    return this;
  }

  public ViewObjectParamsBuilder setLimit(int limit) {
    getParamsBuilder.setLimit(limit);
    return this;
  }

  public ViewObjectParamsBuilder setOffset(int offset) {
    getParamsBuilder.setOffset(offset);
    return this;
  }

  public ViewObjectParamsBuilder addIds(Collection<String> ids) {
    getParamsBuilder.addIds(ids);
    return this;
  }

  public IDs ids() {
    return (id) -> {
      getParamsBuilder.ids().add(id);
      return ViewObjectParamsBuilder.this;
    };
  }

  public ViewObjectParamsBuilder setFieldCollection(FieldCollection fieldCollection) {
    this.fieldCollection = fieldCollection;
    return this;
  }

  public ViewObjectsParams build() {
    return new ViewObjectsParams(getParamsBuilder.build(), fieldCollection);
  }

  public interface OrderFields {
    ViewObjectParamsBuilder add(MetaField field, SortOrder order);
  }

  public interface IDs {
    ViewObjectParamsBuilder add(String id);
  }
}
