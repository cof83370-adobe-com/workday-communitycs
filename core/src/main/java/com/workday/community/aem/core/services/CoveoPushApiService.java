package com.workday.community.aem.core.services;

import java.util.List;
import java.util.Map;

/**
 * The CoveoPushApiService interface.
 */
public interface CoveoPushApiService {

  /**
   * Generate create container api uri.
   *
   * @return The create container api uri
   */
  String generateContainerUri();

  /**
   * Generate batch upload api uri.
   *
   * @param fileId The file id
   * @return The batch upload api uri
   */
  String generateBatchUploadUri(String fileId);

  /**
   * Generate delete all items api uri.
   *
   * @return The delete all items api uri
   */
  String generateDeleteAllItemsUri();

  /**
   * Generate delete single item api uri.
   *
   * @param documentId The document id
   * @return The single item api uri
   */
  String generateDeleteSingleItemUri(String documentId);

  /**
   * Call Api.
   *
   * @param uri        The api url
   * @param header     The api header
   * @param httpMethod The api call method
   * @param payload    The api call payload
   * @return The api response
   */
  Map<String, Object> callApi(String uri, Map<String, String> header, String httpMethod, String payload);

  /**
   * Call batch upload item Api.
   *
   * @param fileId The file id
   * @return The api response
   */
  Map<String, Object> callBatchUploadUri(String fileId);

  /**
   * Call create container Api.
   *
   * @return The api response
   */
  Map<String, Object> callCreateContainerUri();

  /**
   * Call delete all items Api.
   *
   * @return The api status code
   * @see <a href="https://docs.coveo.com/en/131/index-content/deleting-old-items-in-a-push-source">See reference.</a>
   */
  Integer callDeleteAllItemsUri();

  /**
   * Call delete single item Api.
   *
   * @param documentId The document id
   * @return The api status code
   * @see <a href="https://docs.coveo.com/en/171/index-content/deleting-an-item-and-optionally-its-children-in-a-push-source">Link to Coveo documentation.</a>
   */
  Integer callDeleteSingleItemUri(String documentId);

  /**
   * Call Api.
   *
   * @param uploadUri        The upload file api url
   * @param uploadFileHeader The api header
   * @param payload          The api call payload
   * @return The api response
   */
  Map<String, Object> callUploadFileUri(String uploadUri,
                                            Map<String, String> uploadFileHeader,
                                            List<Object> payload);

  /**
   * Indexes a list of items.
   *
   * @param payload The payload
   * @return The api response
   * @see <a href="https://docs.coveo.com/en/90/index-content/manage-batches-of-items-in-a-push-source">Coveo reference.</a>
   */
  Integer indexItems(List<Object> payload);

  /**
   * Transform response.
   *
   * @param response The response
   * @return The transformed response
   */
  Map<String, Object> transformCreateContainerResponse(String response);

  /**
   * Transform payload list to string.
   *
   * @param payload The payload
   * @return The transformed response
   */
  String transformPayload(List<Object> payload);

}
