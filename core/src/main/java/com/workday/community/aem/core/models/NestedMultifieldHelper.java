/*
 * package com.workday.community.aem.core.models;
 * 
 * import java.util.ArrayList; import java.util.Collections; import
 * java.util.Comparator; import java.util.Iterator; import java.util.List;
 * 
 * import javax.annotation.PostConstruct; import javax.inject.Inject; import
 * javax.inject.Named;
 * 
 * import org.apache.commons.lang3.StringUtils; import
 * org.apache.sling.api.SlingHttpServletRequest; import
 * org.apache.sling.api.resource.Resource; import
 * org.apache.sling.api.resource.ValueMap; import
 * org.apache.sling.models.annotations.Default; import
 * org.apache.sling.models.annotations.Model; import
 * org.apache.sling.models.annotations.Optional;
 * 
 * import com.day.cq.wcm.api.Page;
 * 
 * @Model(adaptables = SlingHttpServletRequest.class) public final class
 * NestedMultifieldHelper {
 * 
 * private Iterator<Resource> nodesItemList;
 * 
 * @Inject
 * 
 * @Optional private String componentNodeName;
 * 
 * @Inject private String multifieldNodeName;
 * 
 * @Inject
 * 
 * @Optional
 * 
 * @Default(booleanValues = false) private Boolean inheritProperty;
 * 
 * @Inject
 * 
 * @Optional private String sortByKey;
 * 
 * @Inject
 * 
 * @Named("resourcePage") private Page currentPage;
 * 
 * @Inject private Resource resource;
 * 
 * @Inject
 * 
 * @Optional private String multifieldNodeProperty;
 * 
 * @PostConstruct public void activate() { nodesItemList =
 * Collections.emptyIterator();
 * 
 * if (StringUtils.isBlank(componentNodeName)) { componentNodeName =
 * resource.getName(); } if (!inheritProperty ||
 * Utils.isFragmentPage(currentPage)) { Resource multiFieldNode =
 * resource.getChild(multifieldNodeName); if (multiFieldNode != null) {
 * nodesItemList = multiFieldNode.listChildren(); } } else if
 * (StringUtils.isNotBlank(multifieldNodeName)) { nodesItemList =
 * getMultifieldItemsResources(currentPage); } }
 * 
 *//**
	 * Get the list of children resources of a nested multifield by its property.
	 * 
	 * @return a comma separated string with the items of a nested multifield.
	 */
/*
 * public String getItemsListAsString() { List<String> resourceList = new
 * ArrayList<>(); while (nodesItemList.hasNext()) { ValueMap valueMap =
 * nodesItemList.next().getValueMap();
 * resourceList.add(valueMap.get(multifieldNodeProperty).toString()); } return
 * String.join(",", resourceList); }
 * 
 *//**
	 * Get the list of children resources of a nested multifield.
	 * 
	 * @return a list of resources with the items of a nested multifield.
	 */
/*
 * public Iterator<Resource> getItemsList() { return nodesItemList; }
 *//**
	 * Get the nested multifield children resources. If inheritProperty is TRUE,
	 * then it looks in the parent pages recursively, if not it only looks in the
	 * current page.
	 * 
	 * @param page : page where to look (or start looking) for the nested multifield
	 *             node.
	 * @return the children resources of the multifield's items.
	 *//*
		 * private Iterator<Resource> getMultifieldItemsResources(Page page) { if (page
		 * != null) { Resource contentResource = page.getContentResource(); if
		 * (contentResource != null) { Resource componentResource =
		 * contentResource.getChild(componentNodeName); if (componentResource != null &&
		 * componentResource.getChild(multifieldNodeName) != null) { return
		 * getItemsFromResource(componentResource); } } if (inheritProperty) { return
		 * getMultifieldItemsResources(page.getParent()); } } return
		 * Collections.emptyIterator(); }
		 * 
		 * private Iterator<Resource> getItemsFromResource(Resource componentResource) {
		 * if (!StringUtils.isBlank(sortByKey)) { return
		 * sortListByKey(componentResource); } java.util.Optional<Resource>
		 * multifieldChield =
		 * java.util.Optional.ofNullable(componentResource.getChild(multifieldNodeName))
		 * ; if (multifieldChield.isPresent()) { return
		 * multifieldChield.get().listChildren(); } return Collections.emptyIterator();
		 * }
		 * 
		 * private Iterator<Resource> sortListByKey(Resource componentResource) {
		 * java.util.Optional<Resource> multifieldChield =
		 * java.util.Optional.ofNullable(componentResource.getChild(multifieldNodeName))
		 * ; Iterator<Resource> languageIterator = Collections.emptyIterator(); if
		 * (multifieldChield.isPresent()) { languageIterator =
		 * multifieldChield.get().listChildren(); }
		 * 
		 * List<Resource> resourceList = new ArrayList<>(); while
		 * (languageIterator.hasNext()) { resourceList.add(languageIterator.next()); }
		 * Collections.sort(resourceList, new ResourceComparator());
		 * 
		 * return resourceList.iterator();
		 * 
		 * }
		 * 
		 * class ResourceComparator implements Comparator<Resource> {
		 * 
		 * @Override public int compare(Resource resource1, Resource resource2) {
		 * 
		 * String property1 = (String) resource1.getValueMap().get(sortByKey); String
		 * property2 = (String) resource2.getValueMap().get(sortByKey); return
		 * property1.toLowerCase().compareTo(property2.toLowerCase()); }
		 * 
		 * } }
		 */