package org.lmnl.rdbms;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.lmnl.Annotation;
import org.lmnl.Document;
import org.lmnl.Layer;
import org.lmnl.QName;
import org.lmnl.QNameRepository;
import org.lmnl.Range;
import org.lmnl.xml.XMLParser;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

public class PersistingXMLParser extends XMLParser {
	private static final Joiner PATH_JOINER = Joiner.on('.');
	private QNameRepository qNameRepository;
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setNameRepository(QNameRepository qNameRepository) {
		this.qNameRepository = qNameRepository;
	}

	protected Annotation startAnnotation(Document d, Layer in, QName name, Map<QName, String> attrs, int start, Iterable<Integer> nodePath) {
		final HashMap<String, String> data = Maps.newHashMap();
		for (Map.Entry<QName, String> attr : attrs.entrySet()) {
			data.put(attr.getKey().toString(), attr.getValue());
		}
		data.put("nodePath", PATH_JOINER.join(nodePath));

		PersistentAnnotation annotation = new PersistentAnnotation();
		annotation.setName(qNameRepository.get(name.getNamespace(), name.getLocalName()));
		annotation.setDocument(d);
		annotation.setOwner(in);
		annotation.setRange(new Range(start, start));
		annotation.setText(((PersistentLayer) in).getText());
		annotation.setSerializableData(data);
		sessionFactory.getCurrentSession().save(annotation);
		return annotation;
	}

	protected void endAnnotation(Annotation annotation, int offset) {
		annotation.getRange().setEnd(offset);
		sessionFactory.getCurrentSession().update(annotation);
	}

	protected void newXMLEventBatch() {
		Session session = sessionFactory.getCurrentSession();
		session.flush();
		session.clear();
	}
}
