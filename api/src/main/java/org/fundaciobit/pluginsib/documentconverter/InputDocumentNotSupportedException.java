package org.fundaciobit.plugins.documentconverter;

/**
 * 
 * @author anadal
 *
 */
public class InputDocumentNotSupportedException extends Exception {

  /**
   * @param message
   * @param cause
   */
  public InputDocumentNotSupportedException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public InputDocumentNotSupportedException(String message) {
    super(message);
  }

}
