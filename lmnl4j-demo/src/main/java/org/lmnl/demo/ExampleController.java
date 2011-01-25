/**
 * Layered Markup and Annotation Language for Java (lmnl4j):
 * implementation of LMNL, a markup language supporting layered and/or
 * overlapping annotations.
 *
 * Copyright (C) 2010 the respective authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lmnl.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.jackson.map.ObjectMapper;
import org.lmnl.Annotation;
import org.lmnl.Document;
import org.lmnl.Range;
import org.lmnl.base.DefaultAnnotation;
import org.lmnl.util.OverlapIndexer;
import org.lmnl.xml.PlainTextXMLFilter;
import org.lmnl.xml.XMLUtils;
import org.lmnl.xml.XMLAttribute;
import org.lmnl.xml.XMLElement;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

@Controller
@RequestMapping("/example/")
public class ExampleController implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@RequestMapping(value = "xml/{id}")
	public void resource2xml(@PathVariable("id") String id, HttpServletResponse response) throws Exception {
		Resource resource = getResource(id);
		Transformer transformer = TransformerFactory.newInstance().newTransformer();

		InputStream in = null;
		try {
			response.setContentType(MediaType.APPLICATION_XML.toString());
			ServletOutputStream out = response.getOutputStream();
			transformer.transform(new StreamSource(in = resource.getInputStream()), new StreamResult(out));
			out.flush();
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	@RequestMapping(value = "document/{id}", headers = { "Accept=application/json" })
	public ModelAndView lmnl2json(@PathVariable("id") String id) throws Exception {
		return new ModelAndView(new LmnlJsonView(get(id)));
	}

	protected Document get(String id) throws SAXException, URISyntaxException, IOException {
		XMLReader xmlReader = new PlainTextXMLFilter()//
				.withLineElements(Sets.newHashSet("front", "titlePart", "pb", "div", "head", "lg", "l", "sp", "speaker", "stage", "lb", "zone", "line"))//
				.withElementOnlyElements(Sets.newHashSet("TEI", "text", "document", "surface", "zone", "subst", "overw"));

		Document document = null;
		InputStream inputStream = null;
		try {
			Resource example = getResource(id);

			InputSource xmlInput = new InputSource();
			xmlInput.setSystemId(new URI("lmnl", id, null).toASCIIString());
			xmlInput.setByteStream(inputStream = example.getInputStream());

			document = XMLUtils.buildDocument(xmlReader, xmlInput);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}

		Iterable<Annotation> handShifts = Iterables.filter(document, HANDSHIFT_PREDICATE);
		SortedMap<Range, List<Annotation>> handIndex = new OverlapIndexer(HANDSHIFT_PREDICATE).apply(handShifts);
		for (Range handSegment : handIndex.keySet()) {
			List<Annotation> currentHandShifts = handIndex.get(handSegment);
			if (currentHandShifts.isEmpty()) {
				continue;
			}
			Annotation handShift = currentHandShifts.iterator().next();
			if (handShift.address().start == handSegment.start) {
				String handValue = "";
				for (XMLAttribute attr : ((XMLElement) handShift).getAttributes()) {
					if ("new".equals(attr.localName)) {
						handValue = attr.value;
						break;
					}
				}
				document.add(Document.LMNL_PREFIX, "hand", handValue, new Range(handSegment), DefaultAnnotation.class);
			}
		}
		return document;
	}

	protected Resource getResource(String id) {
		return applicationContext.getResource("/WEB-INF/examples/" + id + ".xml");
	}

	private static class LmnlJsonView extends AbstractView {
		private static final ObjectMapper OM = new ObjectMapper();
		private final Document document;

		public LmnlJsonView(Document document) {
			this.document = document;
		}

		@Override
		protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
			response.setContentType(MediaType.APPLICATION_JSON.toString());
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			OM.writeValue(out, document);
			out.flush();
		}
	}

	private static final Predicate<Annotation> HANDSHIFT_PREDICATE = new Predicate<Annotation>() {

		public boolean apply(Annotation input) {
			return "handShift".equals(input.getLocalName());
		}
	};
}
