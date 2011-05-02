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
import org.lmnl.Annotation;
import org.lmnl.QName;
import org.lmnl.QNameRepository;
import org.lmnl.Range;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class RelationalAnnotationFactory {
	public static final Joiner ANCESTOR_JOINER = Joiner.on('.');
	
	private SessionFactory sessionFactory;

	private QNameRepository nameRepository;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setNameRepository(QNameRepository nameRepository) {
		this.nameRepository = nameRepository;
	}

	public static String getAncestorPath(AnnotationRelation annotation) {
		return (annotation == null ? "" : ANCESTOR_JOINER.join(annotation.getAncestors(), annotation.getId()));
	}
	
	public AnnotationRelation create(Annotation owner, QName name, Range range, String text) {
		Preconditions.checkArgument(owner == null || owner instanceof AnnotationRelation);
		Preconditions.checkArgument((text != null) || (owner != null), "No text given");

		final AnnotationRelation ownerRelation = (AnnotationRelation) owner;
		final AnnotationRelation empty = new AnnotationRelation();
		empty.setName(nameRepository.get(name));
		empty.setRange(range == null ? Range.NULL : range);
		empty.setOwner(ownerRelation);
		empty.setAncestors(getAncestorPath(ownerRelation));
		empty.setText(text == null ? ownerRelation.getText() : setText(empty, text, true));

		sessionFactory.getCurrentSession().save(empty);
		
		return empty;
	}

	public void delete(Annotation annotation) {
		Preconditions.checkArgument(annotation instanceof AnnotationRelation);
		final AnnotationRelation relation = (AnnotationRelation) annotation;
		final Session session = sessionFactory.getCurrentSession();
		
		final String ancestorPath = ANCESTOR_JOINER.join(relation.getAncestors(), relation.getId());
		final Criteria descendantCriteria = session.createCriteria(AnnotationRelation.class);
		descendantCriteria.add(Restrictions.like("ancestors", ancestorPath, MatchMode.START));
		final ScrollableResults descendants = descendantCriteria.scroll(ScrollMode.FORWARD_ONLY);
		while (descendants.next()) {
			session.delete(descendants.get(0));
		}

		session.delete(session.get(AnnotationRelation.class, relation.getId()));
		
		final Criteria orphanedTextsCriteria = session.createCriteria(TextRelation.class);
		orphanedTextsCriteria.add(Restrictions.isEmpty("annotations"));
		final ScrollableResults orphanedTexts = orphanedTextsCriteria.scroll(ScrollMode.FORWARD_ONLY);
		while (orphanedTexts.next()) {
			session.delete(orphanedTexts.get(0));
		}
	}

	public TextRelation setText(Annotation annotation, String text, boolean createNew) {
		try {
			return setText(annotation, new StringReader(text), text.length(), createNew);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	public TextRelation setText(Annotation annotation, Reader reader, int contentLength, boolean createNew) throws IOException {
		final TextRelation text = createNew ? new TextRelation() : getText(annotation);
		final Session session = sessionFactory.getCurrentSession();

		text.setContent(Hibernate.createClob(reader, contentLength));
		session.saveOrUpdate(text);
		session.flush();
		session.refresh(text);

		if (createNew) {
			((AnnotationRelation) annotation).setText(text);
		}

		return text;
	}

	TextRelation getText(Annotation annotation) {
		Preconditions.checkArgument(annotation instanceof AnnotationRelation);
		final Session session = sessionFactory.getCurrentSession();
		
		Criteria c = session.createCriteria(AnnotationRelation.class);
		c.add(Restrictions.idEq(((AnnotationRelation) annotation).getId()));
		c.setFetchMode("text", FetchMode.JOIN);

		TextRelation text = Preconditions.checkNotNull((AnnotationRelation) c.uniqueResult()).getText();
		session.refresh(text);
		return text;
	}
}
