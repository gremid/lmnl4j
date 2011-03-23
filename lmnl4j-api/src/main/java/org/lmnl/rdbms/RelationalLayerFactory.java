package org.lmnl.rdbms;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.lmnl.Layer;
import org.lmnl.QName;
import org.lmnl.QNameRepository;
import org.lmnl.Range;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class RelationalLayerFactory {

	private SessionFactory sessionFactory;

	private QNameRepository nameRepository;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setNameRepository(QNameRepository nameRepository) {
		this.nameRepository = nameRepository;
	}

	public LayerRelation create(Layer owner, QName name, Range range, String text) {
		Preconditions.checkArgument(owner == null || owner instanceof LayerRelation);
		Preconditions.checkArgument((text != null) || (owner != null), "No text given");

		final LayerRelation empty = new LayerRelation();
		empty.setName(nameRepository.get(name));
		empty.setOwner(owner);
		empty.setRange(range == null ? Range.NULL : range);
		empty.setText(text == null ? ((LayerRelation) owner).getText() : setText(empty, text, true));
		sessionFactory.getCurrentSession().save(empty);
		return empty;
	}

	public TextRelation setText(Layer layer, String text, boolean createNew) {
		try {
			return setText(layer, new StringReader(text), createNew);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	public TextRelation setText(Layer layer, Reader reader, boolean createNew) throws IOException {
		final TextRelation text = createNew ? new TextRelation() : getText(layer);
		final Session session = sessionFactory.getCurrentSession();

		text.setContent(Hibernate.createClob(reader, -1));
		session.saveOrUpdate(text);
		session.flush();
		session.refresh(text);

		if (createNew) {
			((LayerRelation) layer).setText(text);
		}

		return text;
	}

	TextRelation getText(Layer layer) {
		Preconditions.checkArgument(layer instanceof LayerRelation);

		Criteria c = sessionFactory.getCurrentSession().createCriteria(LayerRelation.class);
		c.add(Restrictions.idEq(((LayerRelation) layer).getId()));
		c.setFetchMode("text", FetchMode.JOIN);

		return Preconditions.checkNotNull((LayerRelation) c.uniqueResult()).getText();
	}
}
