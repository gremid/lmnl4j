package org.lmnl.rdbms;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.lmnl.Layer;
import org.lmnl.QName;
import org.lmnl.QNameRepository;
import org.lmnl.Range;
import org.lmnl.xml.XMLParser;

import com.google.common.base.Joiner;

public class RelationalXMLParser extends XMLParser {
	private static final Joiner PATH_JOINER = Joiner.on('.');

	private SessionFactory sessionFactory;
	private QNameRepository nameRepository;
	private RelationalLayerFactory layerFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setNameRepository(QNameRepository nameRepository) {
		this.nameRepository = nameRepository;
	}

	public void setLayerFactory(RelationalLayerFactory layerFactory) {
		this.layerFactory = layerFactory;
	}

	protected Layer startAnnotation(Layer in, QName name, Map<QName, String> attrs, int start, Iterable<Integer> nodePath) {
		attrs.put(XMLParser.NODE_PATH_NAME, PATH_JOINER.join(nodePath));

		LayerRelation annotation = new LayerRelation();
		annotation.setName(nameRepository.get(name));
		annotation.setOwner(in);
		annotation.setAncestors(RelationalLayerFactory.getAncestorPath((LayerRelation) in));
		annotation.setRange(new Range(start, start));
		annotation.setText(((LayerRelation) in).getText());
		annotation.setSerializableData((Serializable) attrs);
		return annotation;
	}

	protected void endAnnotation(Layer annotation, int offset) {
		annotation.getRange().setEnd(offset);
		sessionFactory.getCurrentSession().save(annotation);
	}

	@Override
	protected void newOffsetDeltaRange(Layer offsetDeltas, Range range, int offsetDelta) {
		LayerRelation annotation = layerFactory.create(offsetDeltas, OFFSET_DELTA_NAME, range, null);
		annotation.setSerializableData(offsetDelta);
	}

	protected void newXMLEventBatch() {
		Session session = sessionFactory.getCurrentSession();
		session.flush();
		session.clear();
	}

	@Override
	protected void updateText(Layer layer, Reader reader) throws IOException {
		layerFactory.setText(layer, reader, false);
	}
}
