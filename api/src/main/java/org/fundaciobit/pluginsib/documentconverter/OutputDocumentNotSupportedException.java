package org.fundaciobit.plugins.documentconverter;

/**
 * 
 * @author anadal
 *
 */
public class OutputDocumentNotSupportedException extends Exception {

  /**
   * @param message
   * @param cause
   */
  public OutputDocumentNotSupportedException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public OutputDocumentNotSupportedException(String message) {
    super(message);
  }

}
