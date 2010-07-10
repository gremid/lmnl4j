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

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.lmnl.lom.LmnlAnnotation;
import org.lmnl.lom.LmnlDocument;
import org.lmnl.lom.LmnlRangeAddress;
import org.lmnl.lom.base.DefaultLmnlAnnotation;
import org.lmnl.lom.util.OverlapIndexer;
import org.lmnl.xml.LmnlXmlUtils;
import org.lmnl.xml.sax.PlainTextXmlFilter;
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

	private static final URI TEI_NS = URI.create("http://www.tei-c.org/ns/1.0");

	private ApplicationContext applicationContext;

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

	protected LmnlDocument get(String id) throws SAXException, URISyntaxException, IOException {
		XMLReader xmlReader = new PlainTextXmlFilter()//
				.withLineElements(Sets.newHashSet("front", "titlePart", "pb", "div", "head", "lg", "l", "sp", "speaker", "stage", "lb", "zone", "line"))//
				.withElementOnlyElements(Sets.newHashSet("TEI", "text", "document", "surface", "zone", "subst", "overw"));

		LmnlDocument document = null;
		InputStream inputStream = null;
		try {
			Resource example = getResource(id);

			InputSource xmlInput = new InputSource();
			xmlInput.setSystemId(new URI("lmnl", id, null).toASCIIString());
			xmlInput.setByteStream(inputStream = example.getInputStream());

			document = LmnlXmlUtils.buildDocument(xmlReader, xmlInput);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}

		Iterable<LmnlAnnotation> handShifts = Iterables.filter(document, HANDSHIFT_PREDICATE);
		SortedMap<LmnlRangeAddress, List<LmnlAnnotation>> handIndex = new OverlapIndexer(HANDSHIFT_PREDICATE).apply(handShifts);
		for (LmnlRangeAddress handSegment : handIndex.keySet()) {
			List<LmnlAnnotation> currentHandShifts = handIndex.get(handSegment);
			if (currentHandShifts.isEmpty()) {
				continue;
			}
			LmnlAnnotation handShift = currentHandShifts.iterator().next();
			if (handShift.address().start == handSegment.start) {
				LmnlAnnotation hand = document.add(new DefaultLmnlAnnotation(TEI_NS, "", "hand", null, new LmnlRangeAddress(handSegment)));				
				hand.add(new DefaultLmnlAnnotation(TEI_NS, "", "value", Iterables.find(handShift, HAND_ID).getText(), LmnlRangeAddress.NULL));
			}
		}
		return document;
	}

	protected Resource getResource(String id) {
		return applicationContext.getResource("/WEB-INF/examples/" + id + ".xml");
	}

	private static class LmnlJsonView extends AbstractView {
		private final LmnlDocument document;

		public LmnlJsonView(LmnlDocument document) {
			this.document = document;
		}

		@Override
		protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
			response.setContentType(MediaType.APPLICATION_JSON.toString());
			response.setCharacterEncoding("UTF-8");

			final JsonGenerator jg = new JsonFactory().createJsonGenerator(response.getWriter());
			document.serialize(jg);
			jg.flush();
		}
	}

	private static final Predicate<LmnlAnnotation> HANDSHIFT_PREDICATE = new Predicate<LmnlAnnotation>() {

		@Override
		public boolean apply(LmnlAnnotation input) {
			return "handShift".equals(input.getLocalName());
		}
	};

	private static final Predicate<LmnlAnnotation> HAND_ID = new Predicate<LmnlAnnotation>() {

		@Override
		public boolean apply(LmnlAnnotation input) {
			return "new".equals(input.getLocalName());
		}

	};

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
