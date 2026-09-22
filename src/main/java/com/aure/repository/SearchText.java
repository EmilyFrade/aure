package com.aure.repository;

import java.text.Normalizer;

public final class SearchText {

	public static final String ACCENTED = "áàâãäéèêëíìîïóòôõöúùûüçñÁÀÂÃÄÉÈÊËÍÌÎÏÓÒÔÕÖÚÙÛÜÇÑ";
	public static final String PLAIN = "aaaaaeeeeiiiiooooouuuucnAAAAAEEEEIIIIOOOOOUUUUCN";

	private SearchText() {
	}

	public static String normalize(String value) {
		if (value == null) return "";

		return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase();
	}
}
