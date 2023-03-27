package com.workday.community.aem.core.services;

import java.util.HashMap;
import java.util.List;

/**
 * The CoveoPushApiService interface.
 */
public interface CoveoPushApiService {
    
	/**
	 * Generate create container api uri.
	 *
	 * @return The create container api uri
	 */
    public String generateContainerUri();

	/**
	 * Generate batch upload api uri.
	 *
	 * @param fileId The file id
	 * @return The batch upload api uri
	 */
    public String generateBatchUploadUri(String fileId);

	/**
	 * Generate delete all items api uri.
	 *
	 * @return The delete all items api uri
	 */
    public String generateDeleteAllItemsUri();

	/**
	 * Generate delete single item api uri.
	 *
	 * @param documentId The document id
	 * @return The single item api uri
	 */
    public String generateDeleteSingleItemUri(String documentId);

	/**
	 * Call Api.
	 * 
	 * @param url The api url
	 * @param header  The api header
     * @param httpMethod The api call method
     * @param payload The api call payload
	 * @return The api response
	 */
    public HashMap<String, Object> callApi(String uri, HashMap<String, String> header, String httpMethod, String payload);

	/**
	 * Call batch upload item Api.
	 *
	 * @param fileId The file id
	 * @return The api response
	 */
	public HashMap<String, Object> callBatchUploadUri(String fileId);

	/**
	 * Call create container Api.
	 *
	 * @return The api response
	 */
	public HashMap<String, Object> callCreateContainerUri();

	/**
	 * Call delete all items Api.
	 */
	public void callDeleteAllItemsUri();

	/**
	 * Call delete single item Api.
	 *
	 * @param documentId The document id
	 */
	public void callDeleteSingleItemUri(String documentId);

	/**
	 * Call Api.
	 * 
	 * @param uploadUri The upload file api url
	 * @param uploadFileHeader  The api header
     * @param payload The api call payload
	 * @return The api response
	 */
	public HashMap<String, Object> callUploadFileUri(String uploadUri, HashMap<String, String> uploadFileHeader, List<Object> payload);

	/**
	 * Index items.
	 * 
	 * @param payload The payload
	 * @return The api response
	 */
    public Integer indexItems(List<Object> payload);

	/**
	 * Transform response.
	 * 
	 * @param response The response
	 * @return The transformed response
	 */
	public HashMap<String, Object> transformCreateContainerResponse(String response);

	/**
	 * Transform payload list to string.
	 * 
	 * @param payload The payload
	 * @return The transformed response
	 */
	public String transformPayload(List<Object> payload);
}
