package org.lmnl.rdbms;

import java.io.IOException;
import java.io.StringReader;

import org.hibernate.Criteria;
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
import org.lmnl.TextRepository;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

public class RelationalAnnotationFactory {
	public static final Joiner ANCESTOR_JOINER = Joiner.on('.');
	
	private SessionFactory sessionFactory;

	private QNameRepository nameRepository;

	private TextRepository textRepository;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setNameRepository(QNameRepository nameRepository) {
		this.nameRepository = nameRepository;
	}

	public void setTextRepository(TextRepository textRepository) {
		this.textRepository = textRepository;
	}
	
	public static String getAncestorPath(AnnotationRelation annotation) {
		return (annotation == null ? "" : ANCESTOR_JOINER.join(annotation.getAncestors(), annotation.getId()));
	}
	
	public AnnotationRelation create(Annotation owner, QName name, Range range, String text) {
		Preconditions.checkArgument(owner == null || owner instanceof AnnotationRelation);
		Preconditions.checkArgument((text != null) || (owner != null), "No text given");

		final AnnotationRelation ownerRelation = (AnnotationRelation) owner;
		final AnnotationRelation created = new AnnotationRelation();
		created.setName(nameRepository.get(name));
		created.setRange(range == null ? Range.NULL : range);
		created.setOwner(ownerRelation);
		created.setAncestors(getAncestorPath(ownerRelation));
		
		if (text != null || ownerRelation == null) {
			final TextRelation textRelation = new TextRelation();
			sessionFactory.getCurrentSession().save(textRelation);
			created.setText(textRelation);
		} else {
			created.setText(ownerRelation.getText());	
		}
		
		sessionFactory.getCurrentSession().save(created);
		
		if (text != null) {
			try {
				textRepository.write(created, new StringReader(text), text.length());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		return created;
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
}
