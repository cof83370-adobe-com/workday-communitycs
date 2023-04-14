package com.workday.community.aem.core.services.impl;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.services.QueryService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.servlethelpers.MockSlingHttpServletRequest;
import org.apache.sling.servlethelpers.MockSlingHttpServletResponse;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class BookOperationsServiceImplTest {
  private final BookOperationsServiceImpl bookOperationsServiceImpl = new BookOperationsServiceImpl();

  @Mock
  ResourceResolver resourceResolver;
  MockSlingHttpServletRequest mockSlingRequest;
  MockSlingHttpServletResponse mockSlingResponse;

  /** Query service. */
  @Mock
  private QueryService queryService;

  /** The context. */
  private final AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

  @BeforeEach
  public void setUp() throws Exception {
    context.load().json("/com/workday/community/aem/core/models/impl/BookOperationsServiceImplTestData.json",
        "/content");
    Map<String, Object> parameterMap = new HashMap<>();
    parameterMap.put("bookPathData",
        "[\"/content/workday-community/en-us/thomas-sandbox/accordion-image-test\",\"/content/workday-community/en-us/sprint-17/cmtyaem-341/considerations-when-translating-BIRT-output\",\"/content/workday-community/en-us/thomas-sandbox/test-2/workday-mobile-authentication-and-security-faq\",\"/content/workday-community/en-us/thomas-sandbox/test/getting-started-on-prism-analytics\",\"/content/workday-community/en-us/thomas-sandbox/test-2/cloudLoader-advanced-load-tips-and-tricks\",\"/content/workday-community/en-us/sprint-17/cmtyaem-341\",\"/content/workday-community/en-us/thomas-sandbox/related-information-bug/kits---tools-test\"]");
    parameterMap.put("bookResPath", "/content/book-faq-page");
    context.request().setParameterMap(parameterMap);
    mockSlingRequest = context.request();
    mockSlingResponse = context.response();
    Page currentPage = context.currentResource("/content/book-faq-page").adaptTo(Page.class);
    context.registerService(Page.class, currentPage);
    context.registerService(ResourceResolver.class, resourceResolver);
  }

  /**
   * Test testBookPathOperations.
   */
  @Test
  public void testBookPathOperations() {
    Set<String> activatePaths = bookOperationsServiceImpl.processBookPaths(mockSlingRequest);
    Set<String> expectedPaths = Collections.singleton("/content/workday-community/en-us/sprint-17/cmtyaem-341");
    assertNotEquals(expectedPaths, activatePaths);
  }

  /**
   * Test testBookPathOperations with empty .
   */
  @Test
  public void testBookPathOperationsNull() {
    Map<String, Object> parameterMap = new HashMap<>();

    parameterMap.put("bookPathData",
        null);
    parameterMap.put("bookResPath", "/content/book-faq-page");
    context.request().setParameterMap(parameterMap);
    Set<String> activatePaths = bookOperationsServiceImpl.processBookPaths(mockSlingRequest);
    assertEquals(0, activatePaths.size());
  }

  @Test
  public void testBookPathJsonData() {
    String bookRequestJsonStr = mockSlingRequest.getParameter("bookPathData");
    List<String> actualPathList = bookOperationsServiceImpl.getBookPathListFromJson(bookRequestJsonStr);
    List<String> expectedPathList = Stream.of("/content/workday-community/en-us/thomas-sandbox/accordion-image-test",
        "/content/workday-community/en-us/sprint-17/cmtyaem-341/considerations-when-translating-BIRT-output",
        "/content/workday-community/en-us/thomas-sandbox/test-2/workday-mobile-authentication-and-security-faq",
        "/content/workday-community/en-us/thomas-sandbox/test/getting-started-on-prism-analytics",
        "/content/workday-community/en-us/thomas-sandbox/test-2/cloudLoader-advanced-load-tips-and-tricks",
        "/content/workday-community/en-us/sprint-17/cmtyaem-341",
        "/content/workday-community/en-us/thomas-sandbox/related-information-bug/kits---tools-test")
        .collect(Collectors.toList());
    ;
    assertEquals(expectedPathList, actualPathList);
  }

  @Test
  public void testBookPathJsonData1() {
    List<String> actualPathList = bookOperationsServiceImpl.getBookPathListFromJson("");
    assertEquals(0, actualPathList.size());
  }

  @AfterEach
  public void after() {
    resourceResolver.close();
  }

}
