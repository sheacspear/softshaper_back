package ru.softshaper.staticcontent.sec;

import org.jooq.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import ru.softshaper.datasource.meta.ContentDataSource;
import ru.softshaper.services.meta.FieldType;
import ru.softshaper.staticcontent.meta.StaticContentBase;
import ru.softshaper.storage.jooq.tables.Users;

/**
 * @author ashek
 *
 */
@Component
public class SecUserStaticContent extends StaticContentBase {

  public static final String META_CLASS = "secUser";

  public interface Field {
    String login = "login";
    String password = "password";
    String roles = "roles";
  }

  @Autowired
  private SecUserStaticContent(@Qualifier("data") ContentDataSource<Record> dataSource) {
    super(META_CLASS, "Пользователь", Users.USERS.getName(), dataSource);
    this.registerField(Users.USERS.USERNAME,FieldType.STRING).setName("Логин").setCode(Field.login);
    this.registerField(Users.USERS.PASSWORD,FieldType.STRING).setName("Пароль").setCode(Field.password);
    this.registerField(null,FieldType.MULTILINK).setLinkToMetaClass("secRole").setName("Роли").setCode(Field.roles).setNxMTableName("users_roles");
  }
}
