package org.fundaciobit.plugins.documentconverter.openoffice;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fundaciobit.plugins.documentconverter.ConversionDocumentException;
import org.fundaciobit.plugins.documentconverter.IDocumentConverterPlugin;
import org.fundaciobit.plugins.documentconverter.InputDocumentNotSupportedException;
import org.fundaciobit.plugins.documentconverter.OutputDocumentNotSupportedException;
import org.fundaciobit.plugins.utils.AbstractPluginProperties;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFamily;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.artofsolving.jodconverter.openoffice.converter.StreamOpenOfficeDocumentConverter;

/**
 * Conversor de documents amb OpenOffice
 * 
 * Com arrancar openoffice en Windows com a servidor:
 *    "C:\Archivos de programa\OpenOffice 4\program\soffice.exe" -headless -accept="socket,host=127.0.0.1,port=8100;urp;" -nofirststartwizard
 *     
 * @author anadal
 * 
 */
public class OpenOfficeDocumentConverterPlugin extends AbstractPluginProperties
  implements IDocumentConverterPlugin {

  public static final String BASE_PROP = DOCUMENTCONVERTER_BASE_PROPERTY + "openoffice.";
  
  public static final String HOST_PROPERTY = BASE_PROP + "host";
  public static final String PORT_PROPERTY = BASE_PROP + "port";

  public static final DocumentFormatRegistry DFR;

  static {
    DFR = new DocumentFormatRegistry();
    DFR.addDocumentFormat(new DocumentFormat("JPEG",
        DocumentFamily.DRAWING,"image/jpeg", "jpg"));
    
    DFR.addDocumentFormat(new DocumentFormat("PNG",
        DocumentFamily.DRAWING,"image/png", "png"));

    DFR.addDocumentFormat(new DocumentFormat("GIF",
        DocumentFamily.DRAWING,"image/gif", "gif"));

    DFR.addDocumentFormat(new DocumentFormat("Microsoft Word 2007 XML",
        DocumentFamily.TEXT,
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"));

    DFR.addDocumentFormat(new DocumentFormat("Microsoft Excel 2007 XML",
        DocumentFamily.SPREADSHEET,
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"));

    DFR.addDocumentFormat(new DocumentFormat("Microsoft PowerPoint 2007 XML",
        DocumentFamily.PRESENTATION,
        "application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx"));
  }

  protected final Logger log = Logger.getLogger(OpenOfficeDocumentConverterPlugin.class);


  

  /**
   * 
   */
  public OpenOfficeDocumentConverterPlugin() {
    super();
  }

  /**
   * @param propertyKeyBase
   * @param properties
   */
  public OpenOfficeDocumentConverterPlugin(String propertyKeyBase, Properties properties) {
    super(propertyKeyBase, properties);
  }

  /**
   * @param propertyKeyBase
   */
  public OpenOfficeDocumentConverterPlugin(String propertyKeyBase) {
    super(propertyKeyBase);
  }

  public static class DocumentFormatRegistry extends DefaultDocumentFormatRegistry {
    @Override
    public java.util.List<Object> getDocumentFormats() {
      return super.getDocumentFormats();
    }
  }
  
  
  @Override
  public boolean isFileExtensionSupported(String fileExtension) {
    if (fileExtension == null) {
      return false;
    }
    return DFR.getFormatByFileExtension(fileExtension.toLowerCase()) != null;
    
  }
  
  @Override
  public boolean isMimeTypeSupported(String mimeType) {
    if (mimeType == null) {
      return false;
    }
    return DFR.getFormatByMimeType(mimeType) != null;
  }
  
  
  

  @Override
  public void convertDocumentByExtension(InputStream inputData, String inputFileExtension,
      OutputStream outputData, String outputFileExtension) 
  throws InputDocumentNotSupportedException, OutputDocumentNotSupportedException,
      ConversionDocumentException {

    log.debug("Convertint fitxer amb extensió " + inputFileExtension + " a extensió "
        + outputFileExtension);

    DocumentFormat inputFormat = DFR.getFormatByFileExtension(inputFileExtension);
    DocumentFormat outputFormat = DFR.getFormatByFileExtension(outputFileExtension);

    convertirFitxer(inputData, inputFormat, outputFormat, outputData);
  }
  
  @Override
  public void convertDocumentByMime(InputStream inputData, String inputMime,
      OutputStream outputData, String outputMime) 
  throws InputDocumentNotSupportedException, OutputDocumentNotSupportedException,
  ConversionDocumentException {

    log.debug("Convertint fitxer amb tipus " + inputMime + " a tipus "
        + outputMime);

    DocumentFormat inputFormat = DFR.getFormatByMimeType(inputMime);
    DocumentFormat outputFormat = DFR.getFormatByMimeType(outputMime);

    convertirFitxer(inputData, inputFormat, outputFormat, outputData);
  }

  
  

  protected void convertirFitxer(InputStream inputData, DocumentFormat inputFormat,
      DocumentFormat outputFormat, OutputStream outputData) 
    throws InputDocumentNotSupportedException, OutputDocumentNotSupportedException,
    ConversionDocumentException {

    OpenOfficeConnection connection = null;

    if (inputFormat == null) {
      throw new InputDocumentNotSupportedException(
          "El tipus de document d'entrada no està suportat");
    }

    if (outputFormat == null) {
      throw new OutputDocumentNotSupportedException(
          "El tipus de document de sortida no està suportat");
    }

    log.debug("Convertint fitxer de tipus ]" + inputFormat.getMimeType()
        + "[ a tipus ]" + outputFormat.getMimeType() + "[");

    try {
      // Si no hi ha canvi retornam el mateix fitxer
      if (inputFormat.equals(outputFormat)) {
        byte[] buffer = new byte[4 * 1024];
        int len;
        while ((len = inputData.read(buffer)) != -1) {
          outputData.write(buffer, 0, len);
        }
      }

      // Connexió a la instància d'OpenOffice.org que estigui en marxa
      // (cal tenir en compte de configurar el port al fitxer properties)
      String host = getProperty(HOST_PROPERTY, "localhost");
      String portStr = getProperty(PORT_PROPERTY, "8100");
      int port = Integer.parseInt(portStr);

      log.debug("Connectant amb OpenOffice a l'adreça següent: " + host + ":" + port + " ...");

      connection = new SocketOpenOfficeConnection(host, port);
      connection.connect();
      log.debug("Connectat a OpenOffice");

      // Es realitza la conversió
      log.debug("Realitzant conversió ...");

      DocumentConverter converter = null;
      if (isRemoteOpenOfficeHost(host)) {
        converter = new StreamOpenOfficeDocumentConverter(connection, DFR);
      } else {
        converter = new OpenOfficeDocumentConverter(connection, DFR);
      }

      converter.convert(inputData, inputFormat, outputData, outputFormat);

      log.debug("Conversió realitzada ...");

    } catch (Exception e) {
      final String msg = "Error al convertir document amb OpenOffice: " + e.getMessage();
      log.error(msg, e);
      throw new ConversionDocumentException(msg, e);
    } finally {
      if (connection != null) {
        try {
          // Tanca la connexió oberta
          connection.disconnect();
        } catch (Exception ex) {
          log.error("Error tancant connexió amb  OpenOffice", ex);
        }
      }
    }
  }

  /*
   * Funció auxiliar que retorna un boolea indicant si el host del openOffice es
   * remot o no. Si la variable és localhost, 127.0.0.1, el nombre de host de la
   * maquina local o la ip de la maquina local es retorna false en cas contrari
   * retornam true indicant que el host és remoto.
   */
  private boolean isRemoteOpenOfficeHost(String host) {
    try {
      if (host != null
          && !"".equals(host)
          && !host.toUpperCase().startsWith("LOCALHOST")
          && !host.equals("127.0.0.1")
          && !host.toUpperCase().startsWith(
              InetAddress.getLocalHost().getHostName().toUpperCase())
          && !host.equals(InetAddress.getLocalHost().getHostAddress())) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    }
  }

}