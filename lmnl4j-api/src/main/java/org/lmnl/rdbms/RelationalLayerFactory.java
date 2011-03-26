package org.lmnl.rdbms;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.lmnl.Layer;
import org.lmnl.QName;
import org.lmnl.QNameRepository;
import org.lmnl.Range;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class RelationalLayerFactory {
	public static final Joiner ANCESTOR_JOINER = Joiner.on('.');
	
	private SessionFactory sessionFactory;

	private QNameRepository nameRepository;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setNameRepository(QNameRepository nameRepository) {
		this.nameRepository = nameRepository;
	}

	public static String getAncestorPath(LayerRelation layer) {
		return (layer == null ? "" : ANCESTOR_JOINER.join(layer.getAncestors(), layer.getId()));
	}
	
	public LayerRelation create(Layer owner, QName name, Range range, String text) {
		Preconditions.checkArgument(owner == null || owner instanceof LayerRelation);
		Preconditions.checkArgument((text != null) || (owner != null), "No text given");

		final LayerRelation ownerRelation = (LayerRelation) owner;
		final LayerRelation empty = new LayerRelation();
		empty.setName(nameRepository.get(name));
		empty.setRange(range == null ? Range.NULL : range);
		empty.setOwner(ownerRelation);
		empty.setAncestors(getAncestorPath(ownerRelation));
		empty.setText(text == null ? ownerRelation.getText() : setText(empty, text, true));

		sessionFactory.getCurrentSession().save(empty);
		
		return empty;
	}

	public void delete(Layer layer) {
		Preconditions.checkArgument(layer instanceof LayerRelation);
		final LayerRelation relation = (LayerRelation) layer;
		final Session session = sessionFactory.getCurrentSession();
		
		final String ancestorPath = ANCESTOR_JOINER.join(relation.getAncestors(), relation.getId());
		final Criteria descendantCriteria = session.createCriteria(LayerRelation.class);
		descendantCriteria.add(Restrictions.like("ancestors", ancestorPath, MatchMode.START));
		final ScrollableResults descendants = descendantCriteria.scroll(ScrollMode.FORWARD_ONLY);
		while (descendants.next()) {
			session.delete(descendants.get(0));
		}

		session.delete(session.get(LayerRelation.class, relation.getId()));
		
		final Criteria orphanedTextsCriteria = session.createCriteria(TextRelation.class);
		orphanedTextsCriteria.add(Restrictions.isEmpty("layers"));
		final ScrollableResults orphanedTexts = orphanedTextsCriteria.scroll(ScrollMode.FORWARD_ONLY);
		while (orphanedTexts.next()) {
			session.delete(orphanedTexts.get(0));
		}
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
		final Session session = sessionFactory.getCurrentSession();
		
		Criteria c = session.createCriteria(LayerRelation.class);
		c.add(Restrictions.idEq(((LayerRelation) layer).getId()));
		c.setFetchMode("text", FetchMode.JOIN);

		TextRelation text = Preconditions.checkNotNull((LayerRelation) c.uniqueResult()).getText();
		session.refresh(text);
		return text;
	}
}
