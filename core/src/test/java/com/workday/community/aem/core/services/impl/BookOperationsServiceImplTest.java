package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.utils.CommonUtils;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class BookOperationsServiceImplTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class BookOperationsServiceImplTest {

  /** The book operations service impl. */
  private final BookOperationsServiceImpl bookOperationsServiceImpl = new BookOperationsServiceImpl();

  /** The resource resolver. */
  @Mock
  ResourceResolver resourceResolver;

  /** Query service. */
  @Mock
  private QueryService queryService;

  /** The context. */
  private final AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setUp() throws Exception {
    context.load().json("/com/workday/community/aem/core/models/impl/BookOperationsServiceImplTestData.json",
        "/content");

    Page currentPage = context.currentResource("/content/book-faq-page").adaptTo(Page.class);
    context.registerService(Page.class, currentPage);
    context.registerService(ResourceResolver.class, resourceResolver);
  }

  /**
   * Test testBookPathOperations.
   */
  @Test
  public void testBookPathOperations() {
    String bookRequestJsonStr = "[\"/content/workday-community/en-us/thomas-sandbox/accordion-image-test\",\"/content/workday-community/en-us/sprint-17/cmtyaem-341/considerations-when-translating-BIRT-output\",\"/content/workday-community/en-us/thomas-sandbox/test-2/workday-mobile-authentication-and-security-faq\",\"/content/workday-community/en-us/thomas-sandbox/test/getting-started-on-prism-analytics\",\"/content/workday-community/en-us/thomas-sandbox/test-2/cloudLoader-advanced-load-tips-and-tricks\",\"/content/workday-community/en-us/sprint-17/cmtyaem-341\",\"/content/workday-community/en-us/thomas-sandbox/related-information-bug/kits---tools-test\"]";
    String bookResourcePath =  "/content/book-faq-page";

    Set<String> activatePaths = bookOperationsServiceImpl.processBookPaths(context.resourceResolver(), bookResourcePath, bookRequestJsonStr );
    Set<String> expectedPaths = Collections.singleton("/content/workday-community/en-us/sprint-17/cmtyaem-341");
    assertNotEquals(expectedPaths, activatePaths);
  }

  /**
   * Test testBookPathOperations with JSON String empty .
   */
  @Test
  public void testBookPathOperationsNull() {
    String bookRequestJsonStr = null;
    String bookResourcePath = "/content/book-faq-page";
    Set<String> activatePaths = bookOperationsServiceImpl.processBookPaths(resourceResolver, bookResourcePath, bookRequestJsonStr);
    assertEquals(0, activatePaths.size());
  }

    /**
   * Test testBookPathOperations with empty Resource path.
   */
  @Test
  public void testBookPathOperationsNullResPath() {
    String bookRequestJsonStr = "[\"/content/workday-community/en-us/thomas-sandbox/accordion-image-test\",\"/content/workday-community/en-us/sprint-17/cmtyaem-341/considerations-when-translating-BIRT-output\",\"/content/workday-community/en-us/thomas-sandbox/test-2/workday-mobile-authentication-and-security-faq\",\"/content/workday-community/en-us/thomas-sandbox/test/getting-started-on-prism-analytics\",\"/content/workday-community/en-us/thomas-sandbox/test-2/cloudLoader-advanced-load-tips-and-tricks\",\"/content/workday-community/en-us/sprint-17/cmtyaem-341\",\"/content/workday-community/en-us/thomas-sandbox/related-information-bug/kits---tools-test\"]";
    String bookResourcePath = null;
    Set<String> activatePaths = bookOperationsServiceImpl.processBookPaths(resourceResolver, bookResourcePath, bookRequestJsonStr);
    assertEquals(0, activatePaths.size());
  }

  /**
   * Test book path json data.
   */
  @Test
  public void testBookPathJsonData() {
    String bookRequestJsonStr = "[\"/content/workday-community/en-us/thomas-sandbox/accordion-image-test\",\"/content/workday-community/en-us/sprint-17/cmtyaem-341/considerations-when-translating-BIRT-output\",\"/content/workday-community/en-us/thomas-sandbox/test-2/workday-mobile-authentication-and-security-faq\",\"/content/workday-community/en-us/thomas-sandbox/test/getting-started-on-prism-analytics\",\"/content/workday-community/en-us/thomas-sandbox/test-2/cloudLoader-advanced-load-tips-and-tricks\",\"/content/workday-community/en-us/sprint-17/cmtyaem-341\",\"/content/workday-community/en-us/thomas-sandbox/related-information-bug/kits---tools-test\"]";
    List<String> actualPathList = CommonUtils.getPathListFromJsonString(bookRequestJsonStr);
    List<String> expectedPathList = Stream.of("/content/workday-community/en-us/thomas-sandbox/accordion-image-test",
        "/content/workday-community/en-us/sprint-17/cmtyaem-341/considerations-when-translating-BIRT-output",
        "/content/workday-community/en-us/thomas-sandbox/test-2/workday-mobile-authentication-and-security-faq",
        "/content/workday-community/en-us/thomas-sandbox/test/getting-started-on-prism-analytics",
        "/content/workday-community/en-us/thomas-sandbox/test-2/cloudLoader-advanced-load-tips-and-tricks",
        "/content/workday-community/en-us/sprint-17/cmtyaem-341",
        "/content/workday-community/en-us/thomas-sandbox/related-information-bug/kits---tools-test")
        .collect(Collectors.toList());
    assertEquals(expectedPathList, actualPathList);
  }

  /**
   * After.
   */
  @AfterEach
  public void after() {
    resourceResolver.close();
  }

}
