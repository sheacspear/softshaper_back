package ru.softshaper.web.admin.view;

/**
 * Хранилище Источник данных для формы
 *
 * @author asheknew
 *
 */
public interface DataSourceFromViewStore {

	/**
	 * @param contentCode
	 * @return
	 */
	DataSourceFromView get(String contentCode);

	/**
	 * @param contentCode
	 * @param dataViewMapper
	 */
	void register(String contentCode, DataSourceFromView dataViewMapper);
	
	/**
	 * @param defaultViewMapper
	 */
	void setDefault(DataSourceFromView defaultViewMapper);
}
