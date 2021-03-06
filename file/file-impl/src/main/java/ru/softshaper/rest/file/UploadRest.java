package ru.softshaper.rest.file;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import ru.softshaper.bean.file.FileObjectBean;
import ru.softshaper.datasource.file.FileObjectDataSource;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author ashek
 *
 */
@Path("/pr/upload")
public class UploadRest {
  private static final Logger log = LoggerFactory.getLogger(UploadRest.class);
  @Autowired
  private FileObjectDataSource fileObjectDataSource;

  /**
   * inject this from spring context
   */
  @PostConstruct
  public void init() {
    SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
  }

  /**
   * @param attachments
   * @return
   */
  @POST
  @Path("/uploadFile")
  @Consumes("multipart/form-data;charset=UTF-8")
  public String uploadFile(List<Attachment> attachments) {
    for (Attachment attachment : attachments) {
      DataHandler handler = attachment.getDataHandler();
      String filename = new String(handler.getName().getBytes(Charset.forName("ISO-8859-1")), Charset.forName("utf-8"));
      InputStream stream = null;
      try {
        stream = handler.getInputStream();
        FileObjectBean fileObject = new FileObjectBean(null, filename, null, null, null);
        return fileObjectDataSource.createFile(fileObject, stream);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (stream != null) {
          try {
            stream.close();
          } catch (IOException e) {
            log.error(e.getMessage(), e);
          }
        }
      }
    }
    return null;
  }

}
