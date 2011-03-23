package org.lmnl.rdbms;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.lmnl.Annotation;
import org.lmnl.AnnotationRepository;
import org.lmnl.Layer;
import org.lmnl.QName;
import org.lmnl.Visitor;

import com.google.common.base.Preconditions;

public class PersistingAnnotationRepository implements AnnotationRepository {

	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@SuppressWarnings("unchecked")
	public Iterable<Annotation> getAnnotations(Layer layer) {
		Preconditions.checkArgument(layer instanceof PersistentLayer);

		final Criteria c = sessionFactory.getCurrentSession().createCriteria(PersistentAnnotation.class);
		c.createCriteria("owner").add(Restrictions.idEq(((PersistentLayer) layer).getId()));
		c.addOrder(Order.asc("range.start")).addOrder(Order.asc("range.end"));
		return c.list();
	}

	@SuppressWarnings("unchecked")
	public Iterable<Annotation> find(Layer layer, QName annotationName) {
		Preconditions.checkArgument(layer instanceof PersistentLayer);
		
		final Criteria c = sessionFactory.getCurrentSession().createCriteria(PersistentAnnotation.class);
		c.createCriteria("owner").add(Restrictions.idEq(((PersistentLayer) layer).getId()));
		c.addOrder(Order.asc("range.start")).addOrder(Order.asc("range.end"));

		PersistingQNameRepository.addRestrictions(c.createCriteria("name"), annotationName.getNamespace(),
				annotationName.getLocalName());

		return c.list();
	}

	public Annotation add(Layer to, Annotation annotation) {
		Preconditions.checkArgument(to instanceof PersistentLayer);
		Preconditions.checkArgument(annotation instanceof PersistentAnnotation);

		final PersistentAnnotation persistentAnnotation = (PersistentAnnotation) annotation;
		persistentAnnotation.setOwner(to);
		if (persistentAnnotation.getText() == null) {
			persistentAnnotation.setText(((PersistentLayer) to).getText());
		}

		sessionFactory.getCurrentSession().update(annotation);
		return annotation;
	}

	public void remove(Layer from, Annotation annotation) {
		Preconditions.checkArgument(from.equals(annotation.getOwner()));
		Preconditions.checkArgument(from instanceof PersistentLayer);
		Preconditions.checkArgument(annotation instanceof PersistentAnnotation);

		((PersistentAnnotation) annotation).setOwner(null);
		sessionFactory.getCurrentSession().saveOrUpdate(annotation);
	}

	public void visit(Layer layer, Visitor visitor) {
		throw new UnsupportedOperationException();
	}
}
