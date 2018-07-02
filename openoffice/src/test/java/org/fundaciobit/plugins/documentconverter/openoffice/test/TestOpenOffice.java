package org.fundaciobit.plugins.documentconverter.openoffice.test;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fundaciobit.plugins.documentconverter.IDocumentConverterPlugin;
import org.fundaciobit.plugins.documentconverter.openoffice.OpenOfficeDocumentConverterPlugin;

/**
 *
 * @author anadal
 *
 */
public class TestOpenOffice {

  public static void main(String[] args) {

    List<Object> objs = OpenOfficeDocumentConverterPlugin.DFR.getDocumentFormats();
    
    Map<String, String> mimeByExtension = new HashMap<String, String>();

    for (Object object : objs) {

      com.artofsolving.jodconverter.DocumentFormat df = (com.artofsolving.jodconverter.DocumentFormat) object;

      System.out.println(df.getFileExtension() + "\t" + df.getFileExtension().toUpperCase()
          + "_" + df.getName().toUpperCase().replace(' ', '-') + "\t" + df.getMimeType());
      
      mimeByExtension.put(df.getFileExtension(), df.getMimeType());
    }

    System.setProperty(OpenOfficeDocumentConverterPlugin.HOST_PROPERTY, "localhost");

    System.setProperty(OpenOfficeDocumentConverterPlugin.PORT_PROPERTY, "8100");
    try {
      String[] files = new String[] { "test.jpg", "test.gif", "test.png",  "test.pptx",
          "test.xlsx", "test2.xlsx", "test.docx", "test2.docx", "test.doc", "test.odt",
          "test.rtf", "test.txt" };

      IDocumentConverterPlugin oodcp = new OpenOfficeDocumentConverterPlugin();

      for (int i = 0; i < 2; i++) {
        boolean isMime = (i == 0);
        System.out.println(" ==================================== ");
        for (String file : files) {
  
          InputStream inputData = oodcp.getClass().getClassLoader().getResourceAsStream(file);
          String inputFileExtension = file.substring(file.lastIndexOf('.') + 1);
  
          System.out.println("Convertint " + file + "(" + inputFileExtension + ")");
  
          final String outputFileExtension = "pdf";
          
          FileOutputStream outputData = new FileOutputStream((isMime? "mime_" : "ext_") + file + "." + outputFileExtension);
          
          if (isMime) {
            String inputMime = mimeByExtension.get(inputFileExtension);
            String outputMime = mimeByExtension.get(outputFileExtension);
            oodcp.convertDocumentByMime(inputData, inputMime , outputData,
                outputMime);
          } else {
            oodcp.convertDocumentByExtension(inputData, inputFileExtension, outputData,
              outputFileExtension);
          }
          inputData.close();
          outputData.flush();
          outputData.close();
  
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
