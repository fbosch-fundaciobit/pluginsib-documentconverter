package org.fundaciobit.plugins.documentconverter;

/**
 * 
 * @author anadal
 *
 */
public class ConversionDocumentException extends Exception {

  /**
   * @param message
   * @param cause
   */
  public ConversionDocumentException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public ConversionDocumentException(String message) {
    super(message);
  }

  
  
  
}
