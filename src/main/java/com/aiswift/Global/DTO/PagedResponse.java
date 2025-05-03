package com.aiswift.Global.DTO;

import java.util.List;

import org.springframework.data.domain.Page;
//Slice<T> -> infinite scrolling - facebook
public class PagedResponse<T> {
	private List<T> content;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean last;
	
	public PagedResponse() {};
	
	public PagedResponse(Page<T> pageData) {
		this.content = pageData.getContent();
		this.page = pageData.getNumber(); // current page number
		this.size = pageData.getSize();
		this.totalElements = pageData.getTotalElements();
		this.totalPages = pageData.getTotalPages();
		this.last = pageData.isLast();
	}
}
